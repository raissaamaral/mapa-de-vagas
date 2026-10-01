CREATE TABLE status_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES application (id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_status_history_application_id ON status_history (application_id);

-- Existing applications get an initial entry with their current status,
-- so every application has at least one history entry
INSERT INTO status_history (application_id, status, changed_at)
SELECT id, status, now()
FROM application;