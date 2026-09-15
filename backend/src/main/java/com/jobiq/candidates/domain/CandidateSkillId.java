package com.jobiq.candidates.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CandidateSkillId implements Serializable {

    private UUID candidateProfileId;
    private UUID skillId;

    public CandidateSkillId() {
    }

    public CandidateSkillId(UUID candidateProfileId, UUID skillId) {
        this.candidateProfileId = candidateProfileId;
        this.skillId = skillId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CandidateSkillId that)) return false;
        return Objects.equals(candidateProfileId, that.candidateProfileId)
                && Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidateProfileId, skillId);
    }
}
