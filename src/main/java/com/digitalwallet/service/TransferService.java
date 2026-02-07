package com.digitalwallet.service;

import com.digitalwallet.dto.TransferRequest;
import com.digitalwallet.dto.TransferResponse;
import com.digitalwallet.exception.InsufficientBalanceException;
import com.digitalwallet.model.LedgerEntry;
import com.digitalwallet.model.Transfer;
import com.digitalwallet.model.Wallet;
import com.digitalwallet.repository.LedgerEntryRepository;
import com.digitalwallet.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletService walletService;
    private final AuditService auditService;
    private final EventPublisherService eventPublisher;

    @Value("${wallet.transfer.max-amount}")
    private BigDecimal maxTransferAmount;

    @Value("${wallet.transfer.min-amount}")
    private BigDecimal minTransferAmount;

    @Value("${wallet.transfer.daily-limit}")
    private BigDecimal dailyLimit;

    @Transactional
    public Mono<TransferResponse> initiateTransfer(TransferRequest request, Long initiatedBy) {
        log.info("Initiating transfer from {} to {} amount {}",
                request.getSourceWalletNumber(), request.getDestinationWalletNumber(), request.getAmount());

        return validateTransfer(request)
                .then(walletService.getWalletByNumber(request.getSourceWalletNumber()))
                .zipWith(walletService.getWalletByNumber(request.getDestinationWalletNumber()))
                .flatMap(wallets -> {
                    Wallet sourceWallet = wallets.getT1();
                    Wallet destinationWallet = wallets.getT2();

                    return validateWallets(sourceWallet, destinationWallet, request.getAmount())
                            .then(checkDailyLimit(sourceWallet.getId(), request.getAmount()))
                            .then(createTransfer(sourceWallet, destinationWallet, request, initiatedBy));
                })
                .flatMap(this::processTransfer)
                .map(this::buildTransferResponse)
                .doOnSuccess(response -> log.info("Transfer initiated successfully: {}", response.getTransferReference()))
                .doOnError(error -> log.error("Transfer initiation failed", error));
    }

    private Mono<Void> validateTransfer(TransferRequest request) {
        if (request.getAmount().compareTo(minTransferAmount) < 0) {
            return Mono.error(new IllegalArgumentException(
                    "Amount must be at least " + minTransferAmount));
        }

        if (request.getAmount().compareTo(maxTransferAmount) > 0) {
            return Mono.error(new IllegalArgumentException(
                    "Amount exceeds maximum transfer limit of " + maxTransferAmount));
        }

        if (request.getSourceWalletNumber().equals(request.getDestinationWalletNumber())) {
            return Mono.error(new IllegalArgumentException(
                    "Source and destination wallets cannot be the same"));
        }

        return Mono.empty();
    }

    private Mono<Void> validateWallets(Wallet source, Wallet destination, BigDecimal amount) {
        if (!"ACTIVE".equals(source.getStatus())) {
            return Mono.error(new IllegalStateException("Source wallet is not active"));
        }

        if (!"ACTIVE".equals(destination.getStatus())) {
            return Mono.error(new IllegalStateException("Destination wallet is not active"));
        }

        if (!source.getCurrency().equals(destination.getCurrency())) {
            return Mono.error(new IllegalArgumentException(
                    "Currency mismatch: source " + source.getCurrency() +
                            " vs destination " + destination.getCurrency()));
        }

        if (source.getBalance().compareTo(amount) < 0) {
            return Mono.error(new InsufficientBalanceException(
                    "Insufficient balance in source wallet"));
        }

        return Mono.empty();
    }

    private Mono<Void> checkDailyLimit(Long walletId, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return transferRepository.getTotalDebitForPeriod(walletId, startOfDay, endOfDay)
                .defaultIfEmpty(BigDecimal.ZERO)
                .flatMap(totalDebit -> {
                    BigDecimal newTotal = totalDebit.add(amount);
                    if (newTotal.compareTo(dailyLimit) > 0) {
                        return Mono.error(new IllegalStateException(
                                "Daily transfer limit exceeded. Limit: " + dailyLimit +
                                        ", Already used: " + totalDebit));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Transfer> createTransfer(Wallet source, Wallet destination,
                                          TransferRequest request, Long initiatedBy) {
        String transferReference = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transfer transfer = Transfer.builder()
                .transferReference(transferReference)
                .sourceWalletId(source.getId())
                .destinationWalletId(destination.getId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(Transfer.Status.PENDING.name())
                .transferType(Transfer.TransferType.P2P.name())
                .description(request.getDescription())
                .initiatedBy(initiatedBy)
                .createdAt(LocalDateTime.now())
                .build();

        return transferRepository.save(transfer)
                .flatMap(savedTransfer ->
                        auditService.logTransferAction(savedTransfer.getId(), "TRANSFER_INITIATED",
                                        initiatedBy, "Transfer created")
                                .then(eventPublisher.publishTransferInitiated(savedTransfer))
                                .thenReturn(savedTransfer)
                );
    }

    @Transactional
    private Mono<Transfer> processTransfer(Transfer transfer) {
        return Mono.just(transfer)
                .flatMap(t -> updateTransferStatus(t.getId(), Transfer.Status.PROCESSING.name()))
                .flatMap(t -> walletService.debitWallet(t.getSourceWalletId(), t.getAmount(), t.getInitiatedBy())
                        .flatMap(sourceWallet -> createLedgerEntry(t, sourceWallet, LedgerEntry.EntryType.DEBIT))
                        .thenReturn(t))
                .flatMap(t -> walletService.creditWallet(t.getDestinationWalletId(), t.getAmount(), t.getInitiatedBy())
                        .flatMap(destWallet -> createLedgerEntry(t, destWallet, LedgerEntry.EntryType.CREDIT))
                        .thenReturn(t))
                .flatMap(t -> completeTransfer(t.getId()))
                .flatMap(t -> {
                    auditService.logTransferAction(t.getId(), "TRANSFER_COMPLETED",
                            t.getInitiatedBy(), "Transfer completed successfully").subscribe();
                    eventPublisher.publishTransferCompleted(t).subscribe();
                    return Mono.just(t);
                })
                .onErrorResume(error -> {
                    log.error("Transfer processing failed: {}", transfer.getTransferReference(), error);
                    return failTransfer(transfer.getId(), error.getMessage())
                            .flatMap(failedTransfer -> {
                                eventPublisher.publishTransferFailed(failedTransfer, error.getMessage()).subscribe();
                                return Mono.error(error);
                            });
                });
    }

    private Mono<LedgerEntry> createLedgerEntry(Transfer transfer, Wallet wallet, LedgerEntry.EntryType entryType) {
        BigDecimal balanceBefore = entryType == LedgerEntry.EntryType.DEBIT ?
                wallet.getBalance().add(transfer.getAmount()) :
                wallet.getBalance().subtract(transfer.getAmount());

        LedgerEntry entry = LedgerEntry.builder()
                .transferId(transfer.getId())
                .walletId(wallet.getId())
                .entryType(entryType.name())
                .amount(transfer.getAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .currency(transfer.getCurrency())
                .description(entryType.name() + " for transfer " + transfer.getTransferReference())
                .createdAt(LocalDateTime.now())
                .build();

        return ledgerEntryRepository.save(entry);
    }

    private Mono<Transfer> updateTransferStatus(Long transferId, String status) {
        return transferRepository.findById(transferId)
                .flatMap(transfer -> {
                    transfer.setStatus(status);
                    return transferRepository.save(transfer);
                });
    }

    private Mono<Transfer> completeTransfer(Long transferId) {
        return transferRepository.findById(transferId)
                .flatMap(transfer -> {
                    transfer.setStatus(Transfer.Status.COMPLETED.name());
                    transfer.setCompletedAt(LocalDateTime.now());
                    return transferRepository.save(transfer);
                });
    }

    private Mono<Transfer> failTransfer(Long transferId, String reason) {
        return transferRepository.findById(transferId)
                .flatMap(transfer -> {
                    transfer.setStatus(Transfer.Status.FAILED.name());
                    transfer.setDescription(transfer.getDescription() + " | Failure reason: " + reason);
                    return transferRepository.save(transfer);
                });
    }

    public Mono<TransferResponse> getTransferStatus(String transferReference) {
        return transferRepository.findByTransferReference(transferReference)
                .map(this::buildTransferResponse)
                .switchIfEmpty(Mono.error(new RuntimeException("Transfer not found: " + transferReference)));
    }

    private TransferResponse buildTransferResponse(Transfer transfer) {
        return TransferResponse.builder()
                .transferId(transfer.getId())
                .transferReference(transfer.getTransferReference())
                .sourceWalletNumber("****") // Masked for security
                .destinationWalletNumber("****") // Masked for security
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency())
                .status(transfer.getStatus())
                .description(transfer.getDescription())
                .initiatedAt(transfer.getCreatedAt())
                .completedAt(transfer.getCompletedAt())
                .message("Transfer " + transfer.getStatus().toLowerCase())
                .build();
    }
}