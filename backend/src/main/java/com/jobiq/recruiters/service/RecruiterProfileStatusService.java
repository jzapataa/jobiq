package com.jobiq.recruiters.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobiq.recruiters.repository.RecruiterProfileRepository;

@Service
public class RecruiterProfileStatusService {

    private final RecruiterProfileRepository repository;

    public RecruiterProfileStatusService(RecruiterProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean existsForUser(UUID userId) {
        return repository.existsByUserId(userId);
    }
}
