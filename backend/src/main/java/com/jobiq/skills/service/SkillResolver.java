package com.jobiq.skills.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.jobiq.skills.domain.Skill;
import com.jobiq.skills.repository.SkillRepository;

@Service
public class SkillResolver {

    private final SkillRepository repository;
    private final TransactionTemplate requiresNew;

    public SkillResolver(SkillRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.requiresNew = new TransactionTemplate(transactionManager);
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public List<Skill> resolveAll(List<String> rawSkills) {
        Map<String, String> canonicalInputs = new LinkedHashMap<>();
        for (String raw : rawSkills) {
            String display = raw.trim();
            canonicalInputs.putIfAbsent(normalize(display), display);
        }

        List<Skill> resolved = new ArrayList<>();
        canonicalInputs.forEach((normalized, display) -> resolved.add(resolve(display, normalized)));
        return List.copyOf(resolved);
    }

    Skill resolve(String displayName, String normalizedName) {
        return repository.findByNormalizedName(normalizedName)
                .orElseGet(() -> createOrRead(displayName, normalizedName));
    }

    private Skill createOrRead(String displayName, String normalizedName) {
        try {
            return Objects.requireNonNull(requiresNew.execute(status ->
                    repository.saveAndFlush(Skill.create(UUID.randomUUID(), displayName, normalizedName))));
        } catch (DataIntegrityViolationException exception) {
            return repository.findByNormalizedName(normalizedName).orElseThrow(() -> exception);
        }
    }

    public static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
