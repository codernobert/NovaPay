package com.digitalwallet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    private String status;
}

