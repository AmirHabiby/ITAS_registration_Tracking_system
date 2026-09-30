ALTER TABLE training_materials
    DROP CONSTRAINT training_materials_training_id_fkey,
    ADD CONSTRAINT fk_training_materials_training
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE;

ALTER TABLE staff_training_access_logs
    DROP CONSTRAINT staff_training_access_logs_training_id_fkey,
    ADD CONSTRAINT fk_staff_training_access_logs_training
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE;

ALTER TABLE training_requirements
    DROP CONSTRAINT training_requirements_training_id_fkey,
    ADD CONSTRAINT fk_training_requirements_training
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE;

ALTER TABLE training_requests
    DROP CONSTRAINT training_requests_training_id_fkey,
    ADD CONSTRAINT fk_training_requests_training
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE;

ALTER TABLE training_enrollments
    DROP CONSTRAINT training_enrollments_training_id_fkey,
    DROP CONSTRAINT training_enrollments_training_request_id_fkey,
    ADD CONSTRAINT fk_training_enrollments_training
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_training_enrollments_training_request
        FOREIGN KEY (training_request_id) REFERENCES training_requests(id) ON DELETE CASCADE;

ALTER TABLE public_training_enrollments
    DROP CONSTRAINT public_training_enrollments_training_id_fkey,
    ADD CONSTRAINT fk_public_training_enrollments_training
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE;

ALTER TABLE assessment_results
    DROP CONSTRAINT assessment_results_training_enrollment_id_fkey,
    ADD CONSTRAINT fk_assessment_results_training_enrollment
        FOREIGN KEY (training_enrollment_id) REFERENCES training_enrollments(id) ON DELETE CASCADE;

ALTER TABLE online_assessments
    DROP CONSTRAINT online_assessments_training_id_fkey,
    ADD CONSTRAINT fk_online_assessments_training
        FOREIGN KEY (training_id) REFERENCES trainings(id) ON DELETE CASCADE;

ALTER TABLE online_assessment_attempts
    DROP CONSTRAINT online_assessment_attempts_assessment_id_fkey,
    DROP CONSTRAINT online_assessment_attempts_training_enrollment_id_fkey,
    DROP CONSTRAINT fk_online_attempt_assessment_training,
    DROP CONSTRAINT fk_online_attempt_enrollment_training,
    ADD CONSTRAINT fk_online_attempt_assessment
        FOREIGN KEY (assessment_id) REFERENCES online_assessments(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_online_attempt_enrollment
        FOREIGN KEY (training_enrollment_id) REFERENCES training_enrollments(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_online_attempt_assessment_training
        FOREIGN KEY (assessment_id, training_id)
        REFERENCES online_assessments(id, training_id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_online_attempt_enrollment_training
        FOREIGN KEY (training_enrollment_id, training_id)
        REFERENCES training_enrollments(id, training_id) ON DELETE CASCADE;

ALTER TABLE online_assessment_results
    DROP CONSTRAINT online_assessment_results_attempt_id_fkey,
    ADD CONSTRAINT fk_online_assessment_results_attempt
        FOREIGN KEY (attempt_id) REFERENCES online_assessment_attempts(id) ON DELETE CASCADE;

ALTER TABLE online_assessment_question_grades
    DROP CONSTRAINT fk_assessment_question_grade_attempt,
    ADD CONSTRAINT fk_assessment_question_grade_attempt
        FOREIGN KEY (attempt_id, assessment_id)
        REFERENCES online_assessment_attempts(id, assessment_id) ON DELETE CASCADE;

ALTER TABLE online_assessment_result_releases
    DROP CONSTRAINT fk_assessment_result_release_version,
    ADD CONSTRAINT fk_assessment_result_release_version
        FOREIGN KEY (result_id, attempt_id)
        REFERENCES online_assessment_results(id, attempt_id) ON DELETE CASCADE;

CREATE OR REPLACE FUNCTION prevent_published_assessment_mutation() RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'DELETE' AND current_setting('app.training_deletion', true) = 'true' THEN
        RETURN OLD;
    END IF;
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

CREATE OR REPLACE FUNCTION prevent_published_assessment_question_mutation() RETURNS trigger AS $$
DECLARE
    parent_status varchar(20);
BEGIN
    IF TG_OP = 'DELETE' AND current_setting('app.training_deletion', true) = 'true' THEN
        RETURN OLD;
    END IF;
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

CREATE OR REPLACE FUNCTION prevent_published_assessment_option_mutation() RETURNS trigger AS $$
DECLARE
    parent_status varchar(20);
    parent_question_id uuid;
BEGIN
    IF TG_OP = 'DELETE' AND current_setting('app.training_deletion', true) = 'true' THEN
        RETURN OLD;
    END IF;
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

CREATE OR REPLACE FUNCTION prevent_assessment_history_mutation() RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'DELETE' AND current_setting('app.training_deletion', true) = 'true' THEN
        RETURN OLD;
    END IF;
    RAISE EXCEPTION 'Assessment grading and release history is immutable'
        USING ERRCODE = '55000';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION prevent_closed_attempt_answer_mutation() RETURNS trigger AS $$
DECLARE
    attempt_status varchar(20);
    target_attempt_id uuid;
BEGIN
    IF TG_OP = 'DELETE' AND current_setting('app.training_deletion', true) = 'true' THEN
        RETURN OLD;
    END IF;
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
