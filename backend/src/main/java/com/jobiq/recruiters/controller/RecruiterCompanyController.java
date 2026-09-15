package com.jobiq.recruiters.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobiq.companies.dto.CompanyRequest;
import com.jobiq.companies.dto.CompanyResponse;
import com.jobiq.recruiters.service.RecruiterProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/recruiter/company")
public class RecruiterCompanyController {

    private final RecruiterProfileService service;

    public RecruiterCompanyController(RecruiterProfileService service) {
        this.service = service;
    }

    @GetMapping
    public CompanyResponse get(@AuthenticationPrincipal Jwt jwt) {
        return service.getOwnCompany(UUID.fromString(jwt.getSubject()));
    }

    @PutMapping
    public CompanyResponse put(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CompanyRequest request) {
        return service.putOwnCompany(UUID.fromString(jwt.getSubject()), request);
    }
}
