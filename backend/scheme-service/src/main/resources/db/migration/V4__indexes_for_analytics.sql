-- Indexes to improve analytics and SLA queries

-- Speeds filtering and paging by application status
CREATE INDEX IF NOT EXISTS idx_scheme_applications_status ON scheme_applications(status);

-- Speeds joins/filtering by scheme_id (e.g., top schemes, scheme-status aggregation)
CREATE INDEX IF NOT EXISTS idx_scheme_applications_scheme_id ON scheme_applications(scheme_id);

-- Speeds queries filtering by applicant user and scheme
CREATE INDEX IF NOT EXISTS idx_scheme_applications_applicant_scheme ON scheme_applications(applicant_user_id, scheme_id);

-- Speeds finding drafts quickly (user + scheme + status)
CREATE INDEX IF NOT EXISTS idx_scheme_applications_applicant_scheme_status ON scheme_applications(applicant_user_id, scheme_id, status);

-- Speeds latest status-change lookup for an application (max(changed_at))
CREATE INDEX IF NOT EXISTS idx_app_status_history_app_changed_at ON scheme_application_status_history(application_id, changed_at DESC);
