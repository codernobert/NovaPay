package com.digitalwallet.service;

import com.digitalwallet.event.TransferEvent;
import com.digitalwallet.event.TransferEventType;
import com.digitalwallet.model.Transfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final ReactiveKafkaProducerTemplate<String, TransferEvent> kafkaTemplate;

    private static final String TRANSFER_TOPIC = "wallet-transfers";

    public Mono<Void> publishTransferEvent(Transfer transfer, TransferEventType eventType, String message) {
        TransferEvent event = TransferEvent.builder()
                .transferId(transfer.getId())
                .transferReference(transfer.getTransferReference())
                .eventType(eventType)
                .sourceWalletId(transfer.getSourceWalletId())
                .destinationWalletId(transfer.getDestinationWalletId())
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency())
                .status(transfer.getStatus())
                .initiatedBy(transfer.getInitiatedBy())
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();

        return kafkaTemplate.send(TRANSFER_TOPIC, transfer.getTransferReference(), event)
                .doOnSuccess(result -> log.info("Published event: {} for transfer: {}",
                        eventType, transfer.getTransferReference()))
                .doOnError(error -> log.error("Failed to publish event for transfer: {}",
                        transfer.getTransferReference(), error))
                .then();
    }

    public Mono<Void> publishTransferInitiated(Transfer transfer) {
        return publishTransferEvent(transfer, TransferEventType.TRANSFER_INITIATED,
                "Transfer initiated successfully");
    }

    public Mono<Void> publishTransferCompleted(Transfer transfer) {
        return publishTransferEvent(transfer, TransferEventType.TRANSFER_COMPLETED,
                "Transfer completed successfully");
    }

    public Mono<Void> publishTransferFailed(Transfer transfer, String reason) {
        return publishTransferEvent(transfer, TransferEventType.TRANSFER_FAILED,
                "Transfer failed: " + reason);
    }
}