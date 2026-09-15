package com.jobiq.skills.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobiq.skills.domain.Skill;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    Optional<Skill> findByNormalizedName(String normalizedName);
}
