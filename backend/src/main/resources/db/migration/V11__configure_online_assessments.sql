ALTER TABLE online_assessments
    ADD COLUMN duration_minutes integer NOT NULL DEFAULT 60,
    ADD COLUMN attempt_limit integer NOT NULL DEFAULT 1,
    ADD COLUMN available_from timestamp,
    ADD COLUMN available_until timestamp,
    ADD COLUMN randomize_questions boolean NOT NULL DEFAULT false,
    ADD COLUMN randomize_options boolean NOT NULL DEFAULT false,
    ADD CONSTRAINT chk_online_assessment_duration CHECK (duration_minutes BETWEEN 1 AND 1440),
    ADD CONSTRAINT chk_online_assessment_attempt_limit CHECK (attempt_limit BETWEEN 1 AND 100),
    ADD CONSTRAINT chk_online_assessment_availability
        CHECK (available_until IS NULL OR available_from IS NULL OR available_until > available_from);

ALTER TABLE online_assessment_questions
    ADD COLUMN grading_rubric varchar(4000),
    DROP CONSTRAINT chk_online_assessment_question_type,
    ADD CONSTRAINT chk_online_assessment_question_type
        CHECK (question_type IN ('MULTIPLE_CHOICE', 'WRITTEN_RESPONSE')),
    ADD CONSTRAINT chk_online_assessment_question_type_rubric
        CHECK (
            (question_type = 'MULTIPLE_CHOICE' AND grading_rubric IS NULL)
            OR
            (question_type = 'WRITTEN_RESPONSE' AND grading_rubric IS NOT NULL)
        );

CREATE INDEX idx_online_assessments_availability
    ON online_assessments(status, available_from, available_until);
