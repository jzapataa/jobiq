package com.jobiq.candidates.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CandidateProfileResponse(
        UUID id,
        String headline,
        String location,
        String bio,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl,
        List<String> skills,
        Instant createdAt,
        Instant updatedAt) {
}
