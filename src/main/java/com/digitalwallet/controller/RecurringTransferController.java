package com.digitalwallet.controller;

import com.digitalwallet.dto.RecurringTransferRequest;
import com.digitalwallet.dto.RecurringTransferResponse;
import com.digitalwallet.security.JwtUtil;
import com.digitalwallet.service.RecurringTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recurring-transfers")
@RequiredArgsConstructor
public class RecurringTransferController {
    
    private final RecurringTransferService recurringTransferService;
    private final JwtUtil jwtUtil;
    
    @PostMapping
    public Mono<ResponseEntity<RecurringTransferResponse>> createRecurringTransfer(
            @Valid @RequestBody RecurringTransferRequest request,
            ServerWebExchange exchange) {
        
        return extractUserId(exchange)
                .flatMap(userId -> recurringTransferService.createRecurringTransfer(request, userId))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build())
                );
    }
    
    @GetMapping("/user")
    public Flux<RecurringTransferResponse> getUserRecurringTransfers(ServerWebExchange exchange) {
        return extractUserId(exchange)
                .flatMapMany(recurringTransferService::getUserRecurringTransfers);
    }
    
    @GetMapping("/user/active")
    public Flux<RecurringTransferResponse> getActiveRecurringTransfers(ServerWebExchange exchange) {
        return extractUserId(exchange)
                .flatMapMany(recurringTransferService::getActiveRecurringTransfers);
    }
    
    @PutMapping("/{recurringId}/pause")
    public Mono<ResponseEntity<RecurringTransferResponse>> pauseRecurringTransfer(
            @PathVariable Long recurringId,
            ServerWebExchange exchange) {
        
        return extractUserId(exchange)
                .flatMap(userId -> recurringTransferService.pauseRecurringTransfer(recurringId, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build())
                );
    }
    
    @PutMapping("/{recurringId}/resume")
    public Mono<ResponseEntity<RecurringTransferResponse>> resumeRecurringTransfer(
            @PathVariable Long recurringId,
            ServerWebExchange exchange) {
        
        return extractUserId(exchange)
                .flatMap(userId -> recurringTransferService.resumeRecurringTransfer(recurringId, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build())
                );
    }
    
    @DeleteMapping("/{recurringId}")
    public Mono<ResponseEntity<RecurringTransferResponse>> cancelRecurringTransfer(
            @PathVariable Long recurringId,
            ServerWebExchange exchange) {
        
        return extractUserId(exchange)
                .flatMap(userId -> recurringTransferService.cancelRecurringTransfer(recurringId, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build())
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