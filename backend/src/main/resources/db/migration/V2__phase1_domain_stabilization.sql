ALTER TABLE agent_delegations
    ADD COLUMN status varchar(40) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE assessment_results
    ADD COLUMN passing_score_at_assessment numeric(5,2) NOT NULL DEFAULT 70.00,
    ADD COLUMN attempt_number integer NOT NULL DEFAULT 1,
    ADD COLUMN result_status varchar(40) NOT NULL DEFAULT 'FAILED';

UPDATE assessment_results
SET result_status = CASE WHEN passed THEN 'PASSED' ELSE 'FAILED' END,
    passing_score_at_assessment = 70.00,
    attempt_number = 1
WHERE result_status IS NULL;

UPDATE agent_delegations
SET status = 'ACTIVE'
WHERE status IS NULL;
