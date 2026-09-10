ALTER TABLE trainings
    ADD COLUMN access_type varchar(40) NOT NULL DEFAULT 'APPROVAL_REQUIRED',
    ADD COLUMN staff_access_password_hash varchar(255);

CREATE TABLE firms (
    id uuid primary key,
    name varchar(180) not null unique,
    email varchar(180) not null unique,
    description varchar(1000),
    active boolean not null default true,
    created_at timestamp not null,
    updated_at timestamp not null
);

ALTER TABLE agent_delegations
    ALTER COLUMN representative_profile_id DROP NOT NULL,
    ADD COLUMN firm_id uuid REFERENCES firms(id);

CREATE TABLE training_materials (
    id uuid primary key,
    training_id uuid not null references trainings(id),
    uploaded_by_user_id uuid not null references users(id),
    title varchar(180) not null,
    description varchar(500),
    material_type varchar(40) not null,
    file_url varchar(1000) not null,
    cloudinary_public_id varchar(255),
    file_size_bytes bigint,
    created_at timestamp not null,
    updated_at timestamp not null
);

CREATE INDEX idx_training_materials_training_id ON training_materials(training_id);
CREATE INDEX idx_training_materials_uploaded_by ON training_materials(uploaded_by_user_id);

CREATE TABLE staff_training_access_logs (
    id uuid primary key,
    training_id uuid not null references trainings(id),
    full_name varchar(160) not null,
    email varchar(160) not null,
    department varchar(120) not null,
    assessment_score numeric(5,2),
    passed boolean default null,
    assessed_at timestamp,
    completed boolean not null default false,
    completed_at timestamp,
    accessed_at timestamp not null default now(),
    created_at timestamp not null,
    updated_at timestamp not null
);

CREATE INDEX idx_staff_access_training ON staff_training_access_logs(training_id);
CREATE INDEX idx_staff_access_email ON staff_training_access_logs(email);

CREATE INDEX idx_delegation_representative ON agent_delegations(representative_profile_id);
CREATE INDEX idx_delegation_firm ON agent_delegations(firm_id);
CREATE INDEX idx_delegation_delegator ON agent_delegations(delegator_profile_id);