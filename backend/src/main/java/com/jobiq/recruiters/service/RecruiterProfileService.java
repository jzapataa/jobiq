package com.jobiq.recruiters.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobiq.companies.dto.CompanyRequest;
import com.jobiq.companies.dto.CompanyResponse;
import com.jobiq.companies.service.CompanyService;
import com.jobiq.recruiters.domain.RecruiterProfile;
import com.jobiq.recruiters.dto.RecruiterProfileRequest;
import com.jobiq.recruiters.dto.RecruiterProfileResponse;
import com.jobiq.recruiters.repository.RecruiterProfileRepository;
import com.jobiq.shared.error.ApiFieldError;
import com.jobiq.shared.error.ProfileNotFoundException;
import com.jobiq.shared.error.ProfileValidationException;

@Service
public class RecruiterProfileService {

    private final RecruiterProfileRepository repository;
    private final CompanyService companyService;
    private final Clock clock;

    public RecruiterProfileService(RecruiterProfileRepository repository, CompanyService companyService, Clock clock) {
        this.repository = repository;
        this.companyService = companyService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public RecruiterProfileResponse getOwnProfile(UUID userId) {
        return response(requireProfile(userId));
    }

    @Transactional
    public UpsertResult putOwnProfile(UUID userId, RecruiterProfileRequest request) {
        RecruiterProfile profile = repository.findByUserId(userId).orElse(null);
        Instant now = clock.instant();

        if (profile != null) {
            profile.updatePosition(request.position().trim(), now);
            return new UpsertResult(false, response(profile));
        }

        if (request.company() == null) {
            throw new ProfileValidationException(List.of(new ApiFieldError("company", "must not be null")));
        }

        UUID companyId = companyService.create(request.company());
        profile = RecruiterProfile.create(UUID.randomUUID(), userId, companyId, request.position().trim(), now);
        repository.saveAndFlush(profile);
        return new UpsertResult(true, response(profile));
    }

    @Transactional(readOnly = true)
    public CompanyResponse getOwnCompany(UUID userId) {
        return companyService.get(requireProfile(userId).getCompanyId());
    }

    @Transactional
    public CompanyResponse putOwnCompany(UUID userId, CompanyRequest request) {
        return companyService.update(requireProfile(userId).getCompanyId(), request);
    }

    private RecruiterProfile requireProfile(UUID userId) {
        return repository.findByUserId(userId).orElseThrow(ProfileNotFoundException::new);
    }

    private RecruiterProfileResponse response(RecruiterProfile profile) {
        return new RecruiterProfileResponse(profile.getId(), profile.getPosition(), profile.getCompanyId(),
                profile.getCreatedAt(), profile.getUpdatedAt());
    }

    public record UpsertResult(boolean created, RecruiterProfileResponse profile) {
    }
}
