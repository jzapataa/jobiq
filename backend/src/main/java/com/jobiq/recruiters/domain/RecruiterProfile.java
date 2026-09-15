package com.jobiq.recruiters.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recruiter_profiles")
public class RecruiterProfile {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, updatable = false)
    private UUID userId;

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(nullable = false, length = 120)
    private String position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RecruiterProfile() {
    }

    private RecruiterProfile(UUID id, UUID userId, UUID companyId, String position, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.companyId = companyId;
        this.position = position;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RecruiterProfile create(UUID id, UUID userId, UUID companyId, String position, Instant now) {
        return new RecruiterProfile(id, userId, companyId, position, now, now);
    }

    public void updatePosition(String position, Instant now) {
        this.position = position;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCompanyId() { return companyId; }
    public String getPosition() { return position; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
