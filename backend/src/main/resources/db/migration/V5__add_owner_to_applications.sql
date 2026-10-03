ALTER TABLE application
    ADD COLUMN user_id BIGINT REFERENCES users (id) ON DELETE CASCADE;

-- Existing applications were created before users existed:
-- they are assigned to the first registered user
UPDATE application
SET user_id = (SELECT MIN(id) FROM users)
WHERE user_id IS NULL;

ALTER TABLE application
    ALTER COLUMN user_id SET NOT NULL;

CREATE INDEX idx_application_user_id ON application (user_id);