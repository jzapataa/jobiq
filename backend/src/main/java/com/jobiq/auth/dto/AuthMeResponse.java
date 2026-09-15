package com.jobiq.auth.dto;

import java.util.UUID;

import com.jobiq.users.domain.UserRole;

public record AuthMeResponse(
        UUID id,
        String email,
        String name,
        UserRole role,
        boolean profileComplete) {
}
