create table public_training_enrollments (
    id uuid primary key,
    public_trainee_id uuid not null,
    training_id uuid not null references trainings(id),
    enrolled_at timestamp not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uq_public_training_enrollment unique (public_trainee_id, training_id)
);

create index idx_public_training_enrollments_trainee
    on public_training_enrollments(public_trainee_id);

create index idx_public_training_enrollments_training
    on public_training_enrollments(training_id);