package com.jobiq.recruiters.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobiq.recruiters.dto.RecruiterProfileRequest;
import com.jobiq.recruiters.dto.RecruiterProfileResponse;
import com.jobiq.recruiters.service.RecruiterProfileService;
import com.jobiq.recruiters.service.RecruiterProfileService.UpsertResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/recruiter/profile")
public class RecruiterProfileController {

    private final RecruiterProfileService service;

    public RecruiterProfileController(RecruiterProfileService service) {
        this.service = service;
    }

    @GetMapping
    public RecruiterProfileResponse get(@AuthenticationPrincipal Jwt jwt) {
        return service.getOwnProfile(UUID.fromString(jwt.getSubject()));
    }

    @PutMapping
    public ResponseEntity<RecruiterProfileResponse> put(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RecruiterProfileRequest request) {
        UpsertResult result = service.putOwnProfile(UUID.fromString(jwt.getSubject()), request);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(result.profile());
    }
}
