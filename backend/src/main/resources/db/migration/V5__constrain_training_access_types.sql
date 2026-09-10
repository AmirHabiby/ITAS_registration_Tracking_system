UPDATE trainings
SET access_type = 'PRIVATE'
WHERE access_type = 'APPROVAL_REQUIRED';

ALTER TABLE trainings
    ALTER COLUMN access_type SET DEFAULT 'PRIVATE',
    ADD CONSTRAINT chk_trainings_access_type
    CHECK (access_type IN ('PRIVATE', 'PUBLIC', 'STAFF'));