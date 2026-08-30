CREATE TABLE training_requirements (
    id uuid primary key,
    training_id uuid not null references trainings(id),
    name varchar(180) not null,
    description varchar(500),
    required boolean not null default true,
    created_at timestamp not null,
    updated_at timestamp not null
);

CREATE INDEX idx_training_requirements_training_id ON training_requirements(training_id);
