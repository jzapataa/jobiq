package com.jobiq.auth.dto;

import com.jobiq.users.domain.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        String email,
        @NotBlank String password,
        @NotBlank @Size(max = 120) String name,
        @NotNull UserRole role) {
}
