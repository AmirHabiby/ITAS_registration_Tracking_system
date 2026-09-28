CREATE TABLE online_assessments (
    id uuid PRIMARY KEY,
    assessment_series_id uuid NOT NULL,
    version_number integer NOT NULL,
    training_id uuid NOT NULL REFERENCES trainings(id),
    created_by_user_id uuid NOT NULL REFERENCES users(id),
    title varchar(180) NOT NULL,
    instructions varchar(2000),
    passing_score numeric(5,2) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'DRAFT',
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT uq_online_assessment_version UNIQUE (assessment_series_id, version_number),
    CONSTRAINT chk_online_assessment_version_positive CHECK (version_number > 0),
    CONSTRAINT chk_online_assessment_passing_score CHECK (passing_score BETWEEN 0 AND 100),
    CONSTRAINT chk_online_assessment_status CHECK (status IN ('DRAFT', 'PUBLISHED'))
);

CREATE INDEX idx_online_assessments_training_status
    ON online_assessments(training_id, status);
CREATE INDEX idx_online_assessments_series
    ON online_assessments(assessment_series_id, version_number DESC);

CREATE TABLE online_assessment_questions (
    id uuid PRIMARY KEY,
    assessment_id uuid NOT NULL REFERENCES online_assessments(id) ON DELETE CASCADE,
    prompt varchar(4000) NOT NULL,
    question_type varchar(30) NOT NULL,
    display_order integer NOT NULL,
    points numeric(7,2) NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT uq_online_assessment_question_order UNIQUE (assessment_id, display_order),
    CONSTRAINT uq_online_assessment_question_version UNIQUE (id, assessment_id),
    CONSTRAINT chk_online_assessment_question_type CHECK (question_type IN ('MULTIPLE_CHOICE')),
    CONSTRAINT chk_online_assessment_question_order CHECK (display_order > 0),
    CONSTRAINT chk_online_assessment_question_points CHECK (points > 0)
);

CREATE INDEX idx_online_assessment_questions_assessment
    ON online_assessment_questions(assessment_id, display_order);

CREATE TABLE online_assessment_options (
    id uuid PRIMARY KEY,
    question_id uuid NOT NULL REFERENCES online_assessment_questions(id) ON DELETE CASCADE,
    text varchar(2000) NOT NULL,
    display_order integer NOT NULL,
    is_correct boolean NOT NULL DEFAULT false,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT uq_online_assessment_option_order UNIQUE (question_id, display_order),
    CONSTRAINT uq_online_assessment_option_question UNIQUE (id, question_id),
    CONSTRAINT chk_online_assessment_option_order CHECK (display_order > 0)
);

CREATE INDEX idx_online_assessment_options_question
    ON online_assessment_options(question_id, display_order);

CREATE TABLE online_assessment_attempts (
    id uuid PRIMARY KEY,
    assessment_id uuid NOT NULL REFERENCES online_assessments(id),
    training_enrollment_id uuid NOT NULL REFERENCES training_enrollments(id),
    attempt_number integer NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at timestamp NOT NULL,
    submitted_at timestamp,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT uq_online_assessment_attempt_number
        UNIQUE (assessment_id, training_enrollment_id, attempt_number),
    CONSTRAINT uq_online_assessment_attempt_version UNIQUE (id, assessment_id),
    CONSTRAINT chk_online_assessment_attempt_number CHECK (attempt_number > 0),
    CONSTRAINT chk_online_assessment_attempt_status
        CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'EXPIRED'))
);

CREATE INDEX idx_online_assessment_attempts_enrollment
    ON online_assessment_attempts(training_enrollment_id, started_at DESC);
CREATE INDEX idx_online_assessment_attempts_status
    ON online_assessment_attempts(status);

CREATE TABLE online_assessment_attempt_answers (
    id uuid PRIMARY KEY,
    attempt_id uuid NOT NULL,
    assessment_id uuid NOT NULL,
    question_id uuid NOT NULL,
    selected_option_id uuid,
    answered_at timestamp NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT uq_online_assessment_attempt_question UNIQUE (attempt_id, question_id),
    CONSTRAINT fk_online_answer_attempt_version
        FOREIGN KEY (attempt_id, assessment_id)
        REFERENCES online_assessment_attempts(id, assessment_id) ON DELETE CASCADE,
    CONSTRAINT fk_online_answer_question_version
        FOREIGN KEY (question_id, assessment_id)
        REFERENCES online_assessment_questions(id, assessment_id),
    CONSTRAINT fk_online_answer_selected_option
        FOREIGN KEY (selected_option_id, question_id)
        REFERENCES online_assessment_options(id, question_id)
);

CREATE INDEX idx_online_assessment_answers_attempt
    ON online_assessment_attempt_answers(attempt_id);

CREATE TABLE online_assessment_results (
    id uuid PRIMARY KEY,
    attempt_id uuid NOT NULL UNIQUE REFERENCES online_assessment_attempts(id),
    points_earned numeric(7,2) NOT NULL,
    total_points numeric(7,2) NOT NULL,
    score_percent numeric(5,2) NOT NULL,
    passed boolean NOT NULL,
    evaluated_at timestamp NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT chk_online_result_points CHECK (points_earned >= 0 AND total_points > 0),
    CONSTRAINT chk_online_result_score CHECK (score_percent BETWEEN 0 AND 100)
);

CREATE FUNCTION prevent_published_assessment_mutation() RETURNS trigger AS $$
BEGIN
    IF OLD.status = 'PUBLISHED' THEN
        RAISE EXCEPTION 'Published assessment versions are immutable'
            USING ERRCODE = '55000';
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_online_assessment_immutable
    BEFORE UPDATE OR DELETE ON online_assessments
    FOR EACH ROW EXECUTE FUNCTION prevent_published_assessment_mutation();

CREATE FUNCTION prevent_published_assessment_question_mutation() RETURNS trigger AS $$
DECLARE
    parent_status varchar(20);
BEGIN
    IF TG_OP <> 'INSERT' THEN
        SELECT status INTO parent_status
        FROM online_assessments
        WHERE id = OLD.assessment_id;
        IF parent_status = 'PUBLISHED' THEN
            RAISE EXCEPTION 'Questions in published assessment versions are immutable'
                USING ERRCODE = '55000';
        END IF;
    END IF;
    IF TG_OP <> 'DELETE' THEN
        SELECT status INTO parent_status
        FROM online_assessments
        WHERE id = NEW.assessment_id;
        IF parent_status = 'PUBLISHED' THEN
            RAISE EXCEPTION 'Questions cannot be added to published assessment versions'
                USING ERRCODE = '55000';
        END IF;
        RETURN NEW;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_online_assessment_question_immutable
    BEFORE INSERT OR UPDATE OR DELETE ON online_assessment_questions
    FOR EACH ROW EXECUTE FUNCTION prevent_published_assessment_question_mutation();

CREATE FUNCTION prevent_published_assessment_option_mutation() RETURNS trigger AS $$
DECLARE
    parent_status varchar(20);
    parent_question_id uuid;
BEGIN
    IF TG_OP <> 'INSERT' THEN
        SELECT assessment_id INTO parent_question_id
        FROM online_assessment_questions
        WHERE id = OLD.question_id;
        SELECT status INTO parent_status
        FROM online_assessments
        WHERE id = parent_question_id;
        IF parent_status = 'PUBLISHED' THEN
            RAISE EXCEPTION 'Options in published assessment versions are immutable'
                USING ERRCODE = '55000';
        END IF;
    END IF;
    IF TG_OP <> 'DELETE' THEN
        SELECT assessment_id INTO parent_question_id
        FROM online_assessment_questions
        WHERE id = NEW.question_id;
        SELECT status INTO parent_status
        FROM online_assessments
        WHERE id = parent_question_id;
        IF parent_status = 'PUBLISHED' THEN
            RAISE EXCEPTION 'Options cannot be added to published assessment versions'
                USING ERRCODE = '55000';
        END IF;
        RETURN NEW;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_online_assessment_option_immutable
    BEFORE INSERT OR UPDATE OR DELETE ON online_assessment_options
    FOR EACH ROW EXECUTE FUNCTION prevent_published_assessment_option_mutation();
