package com.jobiq.candidates.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobiq.candidates.domain.CandidateSkill;
import com.jobiq.candidates.domain.CandidateSkillId;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, CandidateSkillId> {
    List<CandidateSkill> findByCandidateProfileId(UUID candidateProfileId);
    void deleteByCandidateProfileId(UUID candidateProfileId);
}
