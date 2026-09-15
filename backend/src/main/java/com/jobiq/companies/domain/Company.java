package com.jobiq.companies.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 1500)
    private String description;

    @Column(length = 500)
    private String website;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Company() {
    }

    private Company(UUID id, String name, String description, String website, String logoUrl, String location,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.logoUrl = logoUrl;
        this.location = location;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Company create(UUID id, String name, String description, String website, String logoUrl,
            String location, Instant now) {
        return new Company(id, name, description, website, logoUrl, location, now, now);
    }

    public void replace(String name, String description, String website, String logoUrl, String location, Instant now) {
        this.name = name;
        this.description = description;
        this.website = website;
        this.logoUrl = logoUrl;
        this.location = location;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getWebsite() { return website; }
    public String getLogoUrl() { return logoUrl; }
    public String getLocation() { return location; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
