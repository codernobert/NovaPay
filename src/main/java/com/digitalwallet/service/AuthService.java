package com.digitalwallet.service;

import com.digitalwallet.dto.LoginRequest;
import com.digitalwallet.dto.LoginResponse;
import com.digitalwallet.dto.RegisterRequest;
import com.digitalwallet.dto.RegisterResponse;
import com.digitalwallet.model.User;
import com.digitalwallet.repository.UserRepository;
import com.digitalwallet.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findActiveUserByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid username or password")))
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid username or password")))
                .flatMap(user -> {
                    String token = jwtUtil.generateToken(user.getUsername(), user.getId());

                    LoginResponse response = LoginResponse.builder()
                            .token(token)
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .userId(user.getId())
                            .expiresIn(jwtExpiration)
                            .build();

                    return auditService.logUserAction(user.getId(), "LOGIN", user.getId())
                            .thenReturn(response);
                })
                .doOnSuccess(response -> log.info("User logged in successfully: {}", request.getUsername()))
                .doOnError(error -> log.error("Login failed for user: {}", request.getUsername(), error));
    }

    public Mono<User> getUserFromToken(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Invalid token"));
        }
    }

    public Mono<RegisterResponse> register(RegisterRequest request) {
        // Check if username already exists
        return userRepository.existsByUsername(request.getUsername())
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(new RuntimeException("Username already exists"));
                    }
                    // Check if email already exists
                    return userRepository.existsByEmail(request.getEmail())
                            .flatMap(emailExists -> {
                                if (emailExists) {
                                    return Mono.error(new RuntimeException("Email already exists"));
                                }

                                // Create new user
                                User newUser = User.builder()
                                        .username(request.getUsername())
                                        .email(request.getEmail())
                                        .password(passwordEncoder.encode(request.getPassword()))
                                        .firstName(request.getFirstName())
                                        .lastName(request.getLastName())
                                        .status(User.Status.ACTIVE.name())
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                                return userRepository.save(newUser)
                                        .flatMap(savedUser -> {
                                            // Log the registration action
                                            return auditService.logUserAction(savedUser.getId(), "REGISTER", savedUser.getId())
                                                    .thenReturn(RegisterResponse.builder()
                                                            .userId(savedUser.getId())
                                                            .username(savedUser.getUsername())
                                                            .email(savedUser.getEmail())
                                                            .firstName(savedUser.getFirstName())
                                                            .lastName(savedUser.getLastName())
                                                            .message("User registered successfully")
                                                            .status("SUCCESS")
                                                            .build());
                                        });
                            });
                })
                .doOnSuccess(response -> log.info("User registered successfully: {}", request.getUsername()))
                .doOnError(error -> log.error("Registration failed for user: {}", request.getUsername(), error));
    }
}

