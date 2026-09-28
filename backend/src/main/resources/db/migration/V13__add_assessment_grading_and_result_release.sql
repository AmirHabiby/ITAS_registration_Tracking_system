ALTER TABLE online_assessment_results
    DROP CONSTRAINT online_assessment_results_attempt_id_key,
    ADD CONSTRAINT uq_assessment_result_id_attempt UNIQUE (id, attempt_id),
    ADD COLUMN grading_revision bigint NOT NULL DEFAULT 0;

CREATE TABLE online_assessment_question_grades (
    id uuid PRIMARY KEY,
    attempt_id uuid NOT NULL,
    question_id uuid NOT NULL,
    assessment_id uuid NOT NULL,
    grader_user_id uuid NOT NULL REFERENCES users(id),
    awarded_marks numeric(7,2) NOT NULL,
    feedback varchar(4000),
    graded_at timestamp NOT NULL,
    grade_revision bigint NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT chk_assessment_question_grade_marks CHECK (awarded_marks >= 0),
    CONSTRAINT chk_assessment_question_grade_revision CHECK (grade_revision > 0),
    CONSTRAINT uq_assessment_question_grade_revision UNIQUE (attempt_id, grade_revision),
    CONSTRAINT fk_assessment_question_grade_attempt
        FOREIGN KEY (attempt_id, assessment_id)
        REFERENCES online_assessment_attempts(id, assessment_id),
    CONSTRAINT fk_assessment_question_grade_question
        FOREIGN KEY (question_id, assessment_id)
        REFERENCES online_assessment_questions(id, assessment_id)
);

CREATE INDEX idx_assessment_question_grades_latest
    ON online_assessment_question_grades(attempt_id, question_id, grade_revision DESC);

CREATE TABLE online_assessment_result_releases (
    id uuid PRIMARY KEY,
    attempt_id uuid NOT NULL,
    result_id uuid NOT NULL,
    released_by_user_id uuid NOT NULL REFERENCES users(id),
    released_at timestamp NOT NULL,
    release_number integer NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT uq_assessment_result_release_version UNIQUE (attempt_id, result_id),
    CONSTRAINT uq_assessment_result_release_number UNIQUE (attempt_id, release_number),
    CONSTRAINT chk_assessment_result_release_number CHECK (release_number > 0),
    CONSTRAINT fk_assessment_result_release_version
        FOREIGN KEY (result_id, attempt_id)
        REFERENCES online_assessment_results(id, attempt_id)
);

CREATE INDEX idx_assessment_result_release_latest
    ON online_assessment_result_releases(attempt_id, release_number DESC);

CREATE FUNCTION prevent_assessment_history_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Assessment grading and release history is immutable'
        USING ERRCODE = '55000';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_assessment_question_grade_immutable
    BEFORE UPDATE OR DELETE ON online_assessment_question_grades
    FOR EACH ROW EXECUTE FUNCTION prevent_assessment_history_mutation();

CREATE TRIGGER trg_assessment_result_immutable
    BEFORE UPDATE OR DELETE ON online_assessment_results
    FOR EACH ROW EXECUTE FUNCTION prevent_assessment_history_mutation();

CREATE TRIGGER trg_assessment_result_release_immutable
    BEFORE UPDATE OR DELETE ON online_assessment_result_releases
    FOR EACH ROW EXECUTE FUNCTION prevent_assessment_history_mutation();
