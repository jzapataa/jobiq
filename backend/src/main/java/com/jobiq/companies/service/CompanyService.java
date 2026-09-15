package com.jobiq.companies.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobiq.companies.domain.Company;
import com.jobiq.companies.dto.CompanyRequest;
import com.jobiq.companies.dto.CompanyResponse;
import com.jobiq.companies.repository.CompanyRepository;
import com.jobiq.shared.error.ProfileNotFoundException;

@Service
public class CompanyService {

    private final CompanyRepository repository;
    private final Clock clock;

    public CompanyService(CompanyRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public UUID create(CompanyRequest request) {
        Instant now = clock.instant();
        Company company = Company.create(
                UUID.randomUUID(), request.name().trim(), request.description().trim(),
                optional(request.website()), optional(request.logoUrl()), request.location().trim(), now);
        repository.save(company);
        return company.getId();
    }

    @Transactional(readOnly = true)
    public CompanyResponse get(UUID companyId) {
        return response(repository.findById(companyId).orElseThrow(ProfileNotFoundException::new));
    }

    @Transactional
    public CompanyResponse update(UUID companyId, CompanyRequest request) {
        Company company = repository.findById(companyId).orElseThrow(ProfileNotFoundException::new);
        company.replace(
                request.name().trim(), request.description().trim(), optional(request.website()),
                optional(request.logoUrl()), request.location().trim(), clock.instant());
        return response(company);
    }

    private CompanyResponse response(Company company) {
        return new CompanyResponse(company.getId(), company.getName(), company.getDescription(), company.getWebsite(),
                company.getLogoUrl(), company.getLocation(), company.getCreatedAt(), company.getUpdatedAt());
    }

    private String optional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
