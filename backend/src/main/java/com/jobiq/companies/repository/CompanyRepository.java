package com.jobiq.companies.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobiq.companies.domain.Company;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
}
