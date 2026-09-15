CREATE TABLE skills (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    normalized_name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE candidate_skills (
    candidate_profile_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    PRIMARY KEY (candidate_profile_id, skill_id),
    CONSTRAINT fk_candidate_skills_candidate_profile
        FOREIGN KEY (candidate_profile_id) REFERENCES candidate_profiles(id) ON DELETE RESTRICT,
    CONSTRAINT fk_candidate_skills_skill
        FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE RESTRICT
);

CREATE INDEX idx_candidate_skills_candidate_profile_id
    ON candidate_skills(candidate_profile_id);
