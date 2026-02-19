-- Eligibility rules for schemes (minimal)

CREATE TABLE IF NOT EXISTS scheme_eligibility_rules (
    id BIGSERIAL PRIMARY KEY,
    scheme_id BIGINT NOT NULL UNIQUE REFERENCES schemes(id) ON DELETE CASCADE,
    min_age INT,
    max_age INT,
    min_income BIGINT,
    max_income BIGINT,
    gender VARCHAR(16),
    category VARCHAR(16)
);
