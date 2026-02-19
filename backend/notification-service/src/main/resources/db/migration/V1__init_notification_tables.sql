CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    language VARCHAR(10) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    UNIQUE (code, channel, language)
);

CREATE TABLE IF NOT EXISTS notification_template_metadata (
    template_id BIGINT NOT NULL REFERENCES notification_templates(id) ON DELETE CASCADE,
    metadata_key VARCHAR(100) NOT NULL,
    value VARCHAR(500),
    PRIMARY KEY (template_id, metadata_key)
);

CREATE TABLE IF NOT EXISTS notification_requests (
    id UUID PRIMARY KEY,
    source_service VARCHAR(100) NOT NULL,
    reference_id VARCHAR(100) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    language VARCHAR(10) NOT NULL,
    user_id VARCHAR(50),
    priority VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS notification_request_channels (
    request_id UUID NOT NULL REFERENCES notification_requests(id) ON DELETE CASCADE,
    channel VARCHAR(20) NOT NULL,
    PRIMARY KEY (request_id, channel)
);

CREATE TABLE IF NOT EXISTS notification_request_payload (
    request_id UUID NOT NULL REFERENCES notification_requests(id) ON DELETE CASCADE,
    payload_key VARCHAR(100) NOT NULL,
    value VARCHAR(500),
    PRIMARY KEY (request_id, payload_key)
);

CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGSERIAL PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES notification_requests(id) ON DELETE CASCADE,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INT NOT NULL,
    provider_message_id VARCHAR(128),
    error_code VARCHAR(50),
    error_description VARCHAR(500),
    created_at TIMESTAMPTZ,
    last_attempt_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS user_channel_preferences (
    user_id VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    fallback_channel VARCHAR(20),
    dnd_start TIME,
    dnd_end TIME,
    PRIMARY KEY (user_id, channel)
);
