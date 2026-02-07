package com.digitalwallet.controller;

import com.digitalwallet.dto.SavingsGoalRequest;
import com.digitalwallet.dto.SavingsGoalResponse;
import com.digitalwallet.security.JwtUtil;
import com.digitalwallet.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/savings-goals")
@RequiredArgsConstructor
public class SavingsGoalController {
    
    private final SavingsGoalService savingsGoalService;
    private final JwtUtil jwtUtil;
    
    @PostMapping
    public Mono<ResponseEntity<SavingsGoalResponse>> createSavingsGoal(
            @Valid @RequestBody SavingsGoalRequest request,
            ServerWebExchange exchange) {
        
        return extractUserId(exchange)
                .flatMap(userId -> savingsGoalService.createSavingsGoal(request, userId))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build())
                );
    }
    
    @GetMapping("/user")
    public Flux<SavingsGoalResponse> getUserGoals(ServerWebExchange exchange) {
        return extractUserId(exchange)
                .flatMapMany(savingsGoalService::getUserGoals);
    }
    
    @GetMapping("/user/active")
    public Flux<SavingsGoalResponse> getActiveUserGoals(ServerWebExchange exchange) {
        return extractUserId(exchange)
                .flatMapMany(savingsGoalService::getActiveUserGoals);
    }
    
    @GetMapping("/{goalId}")
    public Mono<ResponseEntity<SavingsGoalResponse>> getGoalById(
            @PathVariable Long goalId) {
        return savingsGoalService.getGoalById(goalId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.notFound().build())
                );
    }
    
    @PostMapping("/{goalId}/contribute")
    public Mono<ResponseEntity<SavingsGoalResponse>> contributeToGoal(
            @PathVariable Long goalId,
            @RequestParam BigDecimal amount,
            ServerWebExchange exchange) {
        
        return extractUserId(exchange)
                .flatMap(userId -> savingsGoalService.contributeToGoal(goalId, amount, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build())
                );
    }
    
    @PutMapping("/{goalId}/pause")
    public Mono<ResponseEntity<SavingsGoalResponse>> pauseGoal(
            @PathVariable Long goalId,
            ServerWebExchange exchange) {
        
        return extractUserId(exchange)
                .flatMap(userId -> savingsGoalService.pauseGoal(goalId, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build())
                );
    }
    
    @PutMapping("/{goalId}/resume")
    public Mono<ResponseEntity<SavingsGoalResponse>> resumeGoal(
            @PathVariable Long goalId,
            ServerWebExchange exchange) {
        
        return extractUserId(exchange)
                .flatMap(userId -> savingsGoalService.resumeGoal(goalId, userId))
                .map(ResponseEntity::ok)
                .onErrorResume(error -> 
                    Mono.just(ResponseEntity.badRequest().build())
                );
    }
    
    @DeleteMapping("/{goalId}")
    public Mono<ResponseEntity<SavingsGoalResponse>> cancelGoal(
            @PathVariable Long goalId,
            ServerWebExchange exchange) {
        
        return extractUserId(exchange)
                .flatMap(userId -> savingsGoalService.cancelGoal(goalId, userId))
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