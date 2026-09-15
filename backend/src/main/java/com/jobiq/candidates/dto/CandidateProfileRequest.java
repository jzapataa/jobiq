package com.jobiq.candidates.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CandidateProfileRequest(
        @NotBlank @Size(max = 160) String headline,
        @NotBlank @Size(max = 120) String location,
        @NotBlank @Size(max = 1200) String bio,
        @Size(max = 500) String linkedinUrl,
        @Size(max = 500) String githubUrl,
        @Size(max = 500) String portfolioUrl,
        @NotNull List<@NotBlank @Size(max = 80) String> skills) {
}
