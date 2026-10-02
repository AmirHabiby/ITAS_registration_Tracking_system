ALTER TABLE online_assessments
    ADD COLUMN week_number integer;

ALTER TABLE online_assessments
    ADD CONSTRAINT ck_online_assessment_week_number
        CHECK (week_number IS NULL OR week_number > 0);
