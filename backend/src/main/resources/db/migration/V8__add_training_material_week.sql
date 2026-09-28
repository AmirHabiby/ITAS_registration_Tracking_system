ALTER TABLE training_materials
    ADD COLUMN week_number integer NOT NULL DEFAULT 1;

ALTER TABLE training_materials
    ADD CONSTRAINT chk_training_materials_week_number_positive
    CHECK (week_number > 0);
