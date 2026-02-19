-- Add minimal eligibility fields for scheme eligibility evaluation

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS date_of_birth DATE,
    ADD COLUMN IF NOT EXISTS annual_income BIGINT,
    ADD COLUMN IF NOT EXISTS gender VARCHAR(16),
    ADD COLUMN IF NOT EXISTS category VARCHAR(16);
