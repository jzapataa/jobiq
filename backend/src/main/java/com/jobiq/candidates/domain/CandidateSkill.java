package com.jobiq.candidates.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "candidate_skills")
@IdClass(CandidateSkillId.class)
public class CandidateSkill {

    @Id
    @Column(name = "candidate_profile_id", nullable = false, updatable = false)
    private UUID candidateProfileId;

    @Id
    @Column(name = "skill_id", nullable = false, updatable = false)
    private UUID skillId;

    protected CandidateSkill() {
    }

    private CandidateSkill(UUID candidateProfileId, UUID skillId) {
        this.candidateProfileId = candidateProfileId;
        this.skillId = skillId;
    }

    public static CandidateSkill of(UUID candidateProfileId, UUID skillId) {
        return new CandidateSkill(candidateProfileId, skillId);
    }

    public UUID getCandidateProfileId() { return candidateProfileId; }
    public UUID getSkillId() { return skillId; }
}
