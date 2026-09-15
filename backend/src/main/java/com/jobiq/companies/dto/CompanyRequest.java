package com.jobiq.companies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 1500) String description,
        @Size(max = 500) String website,
        @Size(max = 500) String logoUrl,
        @NotBlank @Size(max = 120) String location) {
}
