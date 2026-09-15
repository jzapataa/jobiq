package com.jobiq.recruiters.dto;

import java.time.Instant;
import java.util.UUID;

public record RecruiterProfileResponse(
        UUID id,
        String position,
        UUID companyId,
        Instant createdAt,
        Instant updatedAt) {
}
