package com.digitalwallet.controller;

import com.digitalwallet.dto.TransferRequest;
import com.digitalwallet.dto.TransferResponse;
import com.digitalwallet.security.JwtUtil;
import com.digitalwallet.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final JwtUtil jwtUtil;

    @PostMapping("/initiate")
    public Mono<ResponseEntity<TransferResponse>> initiateTransfer(
            @Valid @RequestBody TransferRequest request,
            ServerWebExchange exchange) {

        return extractUserId(exchange)
                .flatMap(userId -> transferService.initiateTransfer(request, userId))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.badRequest()
                                .body(TransferResponse.builder()
                                        .message(error.getMessage())
                                        .status("FAILED")
                                        .build()))
                );
    }

    @GetMapping("/{transferReference}/status")
    public Mono<ResponseEntity<TransferResponse>> getTransferStatus(
            @PathVariable String transferReference,
            Authentication authentication) {
        return transferService.getTransferStatus(transferReference)
                .map(ResponseEntity::ok)
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.notFound().build())
                );
    }

    private Mono<Long> extractUserId(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                return Mono.just(userId);
            } catch (Exception e) {
                return Mono.error(new RuntimeException("Invalid token"));
            }
        }
        return Mono.error(new RuntimeException("No authorization token found"));
    }
}