package com.jobiq.companies.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        String description,
        String website,
        String logoUrl,
        String location,
        Instant createdAt,
        Instant updatedAt) {
}
