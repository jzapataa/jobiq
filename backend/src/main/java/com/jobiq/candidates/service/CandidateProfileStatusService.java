package com.jobiq.candidates.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobiq.candidates.repository.CandidateProfileRepository;

@Service
public class CandidateProfileStatusService {

    private final CandidateProfileRepository repository;

    public CandidateProfileStatusService(CandidateProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean existsForUser(UUID userId) {
        return repository.existsByUserId(userId);
    }
}
