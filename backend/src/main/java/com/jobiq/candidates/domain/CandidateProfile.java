package com.jobiq.candidates.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "candidate_profiles")
public class CandidateProfile {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 160)
    private String headline;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(nullable = false, length = 1200)
    private String bio;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CandidateProfile() {
    }

    private CandidateProfile(UUID id, UUID userId, String headline, String location, String bio,
            String linkedinUrl, String githubUrl, String portfolioUrl, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.headline = headline;
        this.location = location;
        this.bio = bio;
        this.linkedinUrl = linkedinUrl;
        this.githubUrl = githubUrl;
        this.portfolioUrl = portfolioUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CandidateProfile create(UUID id, UUID userId, String headline, String location, String bio,
            String linkedinUrl, String githubUrl, String portfolioUrl, Instant now) {
        return new CandidateProfile(id, userId, headline, location, bio, linkedinUrl, githubUrl, portfolioUrl, now, now);
    }

    public void replace(String headline, String location, String bio, String linkedinUrl, String githubUrl,
            String portfolioUrl, Instant now) {
        this.headline = headline;
        this.location = location;
        this.bio = bio;
        this.linkedinUrl = linkedinUrl;
        this.githubUrl = githubUrl;
        this.portfolioUrl = portfolioUrl;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getHeadline() { return headline; }
    public String getLocation() { return location; }
    public String getBio() { return bio; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public String getGithubUrl() { return githubUrl; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
