CREATE TABLE representative_training_material_progress (
    id uuid PRIMARY KEY,
    representative_id uuid NOT NULL REFERENCES representative_profiles(id) ON DELETE CASCADE,
    training_id uuid NOT NULL REFERENCES trainings(id) ON DELETE CASCADE,
    material_id uuid NOT NULL REFERENCES training_materials(id) ON DELETE CASCADE,
    completed_at timestamp NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,
    CONSTRAINT uq_representative_training_material_progress
        UNIQUE (representative_id, material_id)
);

CREATE INDEX idx_representative_training_material_progress_training
    ON representative_training_material_progress(representative_id, training_id);
