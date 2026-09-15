package com.jobiq.recruiters.dto;

import com.jobiq.companies.dto.CompanyRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecruiterProfileRequest(
        @NotBlank @Size(max = 120) String position,
        @Valid CompanyRequest company) {
}
