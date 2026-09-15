package com.jobiq.candidates.controller;

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

import com.jobiq.candidates.dto.CandidateProfileRequest;
import com.jobiq.candidates.dto.CandidateProfileResponse;
import com.jobiq.candidates.service.CandidateProfileService;
import com.jobiq.candidates.service.CandidateProfileService.UpsertResult;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/candidate/profile")
public class CandidateProfileController {

    private final CandidateProfileService service;

    public CandidateProfileController(CandidateProfileService service) {
        this.service = service;
    }

    @GetMapping
    public CandidateProfileResponse get(@AuthenticationPrincipal Jwt jwt) {
        return service.getOwnProfile(UUID.fromString(jwt.getSubject()));
    }

    @PutMapping
    public ResponseEntity<CandidateProfileResponse> put(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CandidateProfileRequest request) {
        UpsertResult result = service.putOwnProfile(UUID.fromString(jwt.getSubject()), request);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(result.profile());
    }
}
