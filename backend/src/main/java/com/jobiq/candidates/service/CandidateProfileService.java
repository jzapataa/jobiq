package com.jobiq.candidates.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobiq.candidates.domain.CandidateProfile;
import com.jobiq.candidates.domain.CandidateSkill;
import com.jobiq.candidates.dto.CandidateProfileRequest;
import com.jobiq.candidates.dto.CandidateProfileResponse;
import com.jobiq.candidates.repository.CandidateProfileRepository;
import com.jobiq.candidates.repository.CandidateSkillRepository;
import com.jobiq.shared.error.ProfileNotFoundException;
import com.jobiq.skills.domain.Skill;
import com.jobiq.skills.repository.SkillRepository;
import com.jobiq.skills.service.SkillResolver;

@Service
public class CandidateProfileService {

    private final CandidateProfileRepository profileRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final SkillRepository skillRepository;
    private final SkillResolver skillResolver;
    private final Clock clock;

    public CandidateProfileService(CandidateProfileRepository profileRepository,
            CandidateSkillRepository candidateSkillRepository,
            SkillRepository skillRepository,
            SkillResolver skillResolver,
            Clock clock) {
        this.profileRepository = profileRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.skillRepository = skillRepository;
        this.skillResolver = skillResolver;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse getOwnProfile(UUID userId) {
        CandidateProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(ProfileNotFoundException::new);
        return response(profile);
    }

    @Transactional
    public UpsertResult putOwnProfile(UUID userId, CandidateProfileRequest request) {
        Instant now = clock.instant();
        CandidateProfile profile = profileRepository.findByUserId(userId).orElse(null);
        boolean created = profile == null;

        if (created) {
            profile = CandidateProfile.create(
                    UUID.randomUUID(), userId,
                    request.headline().trim(), request.location().trim(), request.bio().trim(),
                    optional(request.linkedinUrl()), optional(request.githubUrl()), optional(request.portfolioUrl()), now);
            profileRepository.saveAndFlush(profile);
        } else {
            profile.replace(
                    request.headline().trim(), request.location().trim(), request.bio().trim(),
                    optional(request.linkedinUrl()), optional(request.githubUrl()), optional(request.portfolioUrl()), now);
        }

        replaceSkills(profile.getId(), request.skills());
        return new UpsertResult(created, response(profile));
    }

    private void replaceSkills(UUID profileId, List<String> rawSkills) {
        List<Skill> resolved = skillResolver.resolveAll(rawSkills);
        candidateSkillRepository.deleteByCandidateProfileId(profileId);
        candidateSkillRepository.flush();
        candidateSkillRepository.saveAll(resolved.stream()
                .map(skill -> CandidateSkill.of(profileId, skill.getId()))
                .toList());
    }

    private CandidateProfileResponse response(CandidateProfile profile) {
        List<UUID> skillIds = candidateSkillRepository.findByCandidateProfileId(profile.getId()).stream()
                .map(CandidateSkill::getSkillId)
                .toList();
        List<String> skills = skillRepository.findAllById(skillIds).stream()
                .map(Skill::getName)
                .sorted(Comparator.comparing(String::toLowerCase))
                .toList();
        return new CandidateProfileResponse(
                profile.getId(), profile.getHeadline(), profile.getLocation(), profile.getBio(),
                profile.getLinkedinUrl(), profile.getGithubUrl(), profile.getPortfolioUrl(),
                skills, profile.getCreatedAt(), profile.getUpdatedAt());
    }

    private String optional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    public record UpsertResult(boolean created, CandidateProfileResponse profile) {
    }
}
