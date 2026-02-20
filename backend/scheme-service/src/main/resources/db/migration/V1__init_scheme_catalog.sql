-- Phase 1: Scheme catalog

CREATE SCHEMA IF NOT EXISTS scheme;
SET search_path TO scheme;

CREATE TABLE IF NOT EXISTS schemes (
    id BIGSERIAL PRIMARY KEY,
    scheme_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    department VARCHAR(128),
    benefit_type VARCHAR(64),
    benefit_details TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS scheme_document_requirements (
    id BIGSERIAL PRIMARY KEY,
    scheme_id BIGINT NOT NULL REFERENCES schemes(id) ON DELETE CASCADE,
    doc_type VARCHAR(64) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (scheme_id, doc_type)
);

CREATE INDEX IF NOT EXISTS idx_scheme_doc_req_scheme_id ON scheme_document_requirements(scheme_id);

-- Phase 1: Scheme application workflow

CREATE TABLE IF NOT EXISTS scheme_applications (
    id BIGSERIAL PRIMARY KEY,
    scheme_id BIGINT NOT NULL REFERENCES schemes(id) ON DELETE RESTRICT,
    applicant_user_id VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    assigned_officer_id VARCHAR(128),
    submitted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_scheme_applications_scheme_id ON scheme_applications(scheme_id);
CREATE INDEX IF NOT EXISTS idx_scheme_applications_applicant_user_id ON scheme_applications(applicant_user_id);
CREATE INDEX IF NOT EXISTS idx_scheme_applications_status ON scheme_applications(status);

CREATE TABLE IF NOT EXISTS scheme_application_documents (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES scheme_applications(id) ON DELETE CASCADE,
    doc_type VARCHAR(64) NOT NULL,
    file_ref TEXT,
    verified_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    remarks TEXT,
    UNIQUE(application_id, doc_type)
);

CREATE INDEX IF NOT EXISTS idx_scheme_app_docs_application_id ON scheme_application_documents(application_id);

CREATE TABLE IF NOT EXISTS scheme_application_status_history (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES scheme_applications(id) ON DELETE CASCADE,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    changed_by VARCHAR(128) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    remarks TEXT
);

CREATE INDEX IF NOT EXISTS idx_scheme_app_status_history_application_id ON scheme_application_status_history(application_id);
