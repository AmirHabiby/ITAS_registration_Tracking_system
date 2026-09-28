ALTER TABLE online_assessment_attempts
    ADD COLUMN deadline_at timestamp,
    ADD COLUMN expired_at timestamp;

UPDATE online_assessment_attempts attempt
SET deadline_at = attempt.started_at + assessment.duration_minutes * INTERVAL '1 minute'
FROM online_assessments assessment
WHERE assessment.id = attempt.assessment_id;

ALTER TABLE online_assessment_attempts
    ALTER COLUMN deadline_at SET NOT NULL;

ALTER TABLE online_assessment_attempt_answers
    ADD COLUMN response_text varchar(4000),
    ADD CONSTRAINT chk_online_answer_single_response
        CHECK (selected_option_id IS NULL OR response_text IS NULL);

ALTER TABLE online_assessment_results
    ADD COLUMN is_final boolean NOT NULL DEFAULT true;

CREATE TABLE online_assessment_attempt_question_order (
    id uuid PRIMARY KEY,
    attempt_id uuid NOT NULL REFERENCES online_assessment_attempts(id) ON DELETE CASCADE,
    question_id uuid NOT NULL REFERENCES online_assessment_questions(id),
    assessment_id uuid NOT NULL,
    display_order integer NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT uq_attempt_question_snapshot_position UNIQUE (attempt_id, display_order),
    CONSTRAINT uq_attempt_question_snapshot_question UNIQUE (attempt_id, question_id),
    CONSTRAINT chk_attempt_question_snapshot_order CHECK (display_order > 0),
    CONSTRAINT fk_attempt_question_snapshot_assessment
        FOREIGN KEY (attempt_id, assessment_id)
        REFERENCES online_assessment_attempts(id, assessment_id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_question_snapshot_question
        FOREIGN KEY (question_id, assessment_id)
        REFERENCES online_assessment_questions(id, assessment_id)
);

INSERT INTO online_assessment_attempt_question_order
    (id, attempt_id, question_id, assessment_id, display_order, created_at, updated_at)
SELECT
    md5(attempt.id::text || question.id::text)::uuid,
    attempt.id,
    question.id,
    attempt.assessment_id,
    question.display_order,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM online_assessment_attempts attempt
JOIN online_assessment_questions question ON question.assessment_id = attempt.assessment_id;

CREATE TABLE online_assessment_attempt_option_order (
    id uuid PRIMARY KEY,
    attempt_id uuid NOT NULL REFERENCES online_assessment_attempts(id) ON DELETE CASCADE,
    question_id uuid NOT NULL REFERENCES online_assessment_questions(id),
    option_id uuid NOT NULL REFERENCES online_assessment_options(id),
    assessment_id uuid NOT NULL,
    display_order integer NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT uq_attempt_option_snapshot_position UNIQUE (attempt_id, question_id, display_order),
    CONSTRAINT uq_attempt_option_snapshot_option UNIQUE (attempt_id, question_id, option_id),
    CONSTRAINT chk_attempt_option_snapshot_order CHECK (display_order > 0),
    CONSTRAINT fk_attempt_option_snapshot_assessment
        FOREIGN KEY (attempt_id, assessment_id)
        REFERENCES online_assessment_attempts(id, assessment_id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_option_snapshot_question
        FOREIGN KEY (question_id, assessment_id)
        REFERENCES online_assessment_questions(id, assessment_id),
    CONSTRAINT fk_attempt_option_snapshot_option
        FOREIGN KEY (option_id, question_id)
        REFERENCES online_assessment_options(id, question_id)
);

INSERT INTO online_assessment_attempt_option_order
    (id, attempt_id, question_id, option_id, assessment_id, display_order, created_at, updated_at)
SELECT
    md5(attempt.id::text || option.id::text)::uuid,
    attempt.id,
    question.id,
    option.id,
    attempt.assessment_id,
    option.display_order,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM online_assessment_attempts attempt
JOIN online_assessment_questions question ON question.assessment_id = attempt.assessment_id
JOIN online_assessment_options option ON option.question_id = question.id;

CREATE UNIQUE INDEX uq_online_attempt_active_per_enrollment
    ON online_assessment_attempts (assessment_id, training_enrollment_id)
    WHERE status = 'IN_PROGRESS';

CREATE INDEX idx_online_assessment_attempts_deadline
    ON online_assessment_attempts(status, deadline_at);

CREATE FUNCTION prevent_closed_attempt_answer_mutation() RETURNS trigger AS $$
DECLARE
    attempt_status varchar(20);
    target_attempt_id uuid;
BEGIN
    IF TG_OP = 'DELETE' THEN
        target_attempt_id := OLD.attempt_id;
    ELSE
        target_attempt_id := NEW.attempt_id;
    END IF;
    SELECT status INTO attempt_status
    FROM online_assessment_attempts
    WHERE id = target_attempt_id;
    IF attempt_status <> 'IN_PROGRESS' THEN
        RAISE EXCEPTION 'Answers for closed assessment attempts are immutable'
            USING ERRCODE = '55000';
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_closed_attempt_answer_immutable
    BEFORE INSERT OR UPDATE OR DELETE ON online_assessment_attempt_answers
    FOR EACH ROW EXECUTE FUNCTION prevent_closed_attempt_answer_mutation();
