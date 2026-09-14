package com.jobiq.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        String email,
        @NotBlank String password) {
}
