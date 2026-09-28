ALTER TABLE online_assessments
    ADD CONSTRAINT uq_online_assessment_id_training UNIQUE (id, training_id);

ALTER TABLE training_enrollments
    ADD CONSTRAINT uq_training_enrollment_id_training UNIQUE (id, training_id);

ALTER TABLE online_assessment_attempts
    ADD COLUMN training_id uuid;

UPDATE online_assessment_attempts attempt
SET training_id = assessment.training_id
FROM online_assessments assessment
WHERE assessment.id = attempt.assessment_id;

ALTER TABLE online_assessment_attempts
    ALTER COLUMN training_id SET NOT NULL,
    ADD CONSTRAINT fk_online_attempt_assessment_training
        FOREIGN KEY (assessment_id, training_id)
        REFERENCES online_assessments(id, training_id),
    ADD CONSTRAINT fk_online_attempt_enrollment_training
        FOREIGN KEY (training_enrollment_id, training_id)
        REFERENCES training_enrollments(id, training_id);
