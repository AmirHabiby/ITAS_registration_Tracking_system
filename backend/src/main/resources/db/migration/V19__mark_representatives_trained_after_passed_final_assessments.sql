WITH passed_final_results AS (
    SELECT DISTINCT ON (attempt.training_enrollment_id)
        attempt.training_enrollment_id,
        result.score_percent,
        assessment.title AS assessment_title,
        result_release.released_at
    FROM online_assessment_attempts attempt
    JOIN online_assessments assessment
        ON assessment.id = attempt.assessment_id
    JOIN online_assessment_results result
        ON result.attempt_id = attempt.id
       AND result.is_final = TRUE
       AND result.passed = TRUE
    JOIN online_assessment_result_releases result_release
        ON result_release.attempt_id = attempt.id
       AND result_release.result_id = result.id
    WHERE assessment.week_number IS NULL
    ORDER BY attempt.training_enrollment_id, result_release.released_at DESC
)
UPDATE training_enrollments enrollment
SET assessment_score = passed.score_percent,
    passed = TRUE,
    assessment_note = passed.assessment_title,
    assessed_at = passed.released_at,
    status = 'COMPLETED',
    updated_at = CURRENT_TIMESTAMP
FROM passed_final_results passed
WHERE enrollment.id = passed.training_enrollment_id;

UPDATE representative_profiles representative
SET status = 'TRAINED',
    updated_at = CURRENT_TIMESTAMP
WHERE EXISTS (
    SELECT 1
    FROM training_enrollments enrollment
    JOIN online_assessment_attempts attempt
        ON attempt.training_enrollment_id = enrollment.id
    JOIN online_assessments assessment
        ON assessment.id = attempt.assessment_id
    JOIN online_assessment_results result
        ON result.attempt_id = attempt.id
       AND result.is_final = TRUE
       AND result.passed = TRUE
    JOIN online_assessment_result_releases result_release
        ON result_release.attempt_id = attempt.id
       AND result_release.result_id = result.id
    WHERE enrollment.representative_id = representative.id
      AND assessment.week_number IS NULL
);
