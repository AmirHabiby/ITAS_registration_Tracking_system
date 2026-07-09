create table users (
    id uuid primary key,
    username varchar(120) not null unique,
    password_hash varchar(255) not null,
    role varchar(40) not null,
    enabled boolean not null,
    display_name varchar(160) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table representative_profiles (
    id uuid primary key,
    user_id uuid not null unique references users(id),
    full_name varchar(160) not null,
    email varchar(160) not null unique,
    status varchar(40) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table delegator_profiles (
    id uuid primary key,
    user_id uuid not null unique references users(id),
    full_name varchar(160) not null,
    email varchar(160) not null unique,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table training_institute_profiles (
    id uuid primary key,
    user_id uuid not null unique references users(id),
    name varchar(180) not null,
    contact_email varchar(180) not null,
    active boolean not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table trainings (
    id uuid primary key,
    training_institute_profile_id uuid not null references training_institute_profiles(id),
    title varchar(180) not null,
    description varchar(1000) not null,
    passing_score numeric(5,2) not null default 70,
    allowed_retake_attempts integer not null default 0,
    capacity integer not null,
    start_date date not null,
    end_date date not null,
    status varchar(40) not null,
    active boolean not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table training_requests (
    id uuid primary key,
    representative_id uuid not null references representative_profiles(id),
    training_id uuid not null references trainings(id),
    status varchar(40) not null,
    approved_by_delegator_id uuid references delegator_profiles(id),
    rejected_by_delegator_id uuid references delegator_profiles(id),
    approved_at timestamp,
    rejected_at timestamp,
    reviewer_username varchar(120),
    reviewer_note varchar(500),
    requested_at timestamp not null,
    reviewed_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table training_enrollments (
    id uuid primary key,
    representative_id uuid not null references representative_profiles(id),
    training_id uuid not null references trainings(id),
    training_request_id uuid references training_requests(id),
    status varchar(40) not null,
    assessment_score numeric(5,2),
    passed boolean,
    assessment_note varchar(500),
    assessed_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uq_training_enrollment unique (representative_id, training_id)
);

create table assessment_results (
    id uuid primary key,
    training_enrollment_id uuid not null references training_enrollments(id),
    submitted_by_user_id uuid not null references users(id),
    score numeric(5,2) not null,
    passed boolean not null,
    remarks varchar(500),
    assessment_date timestamp not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table agent_delegations (
    id uuid primary key,
    representative_profile_id uuid not null references representative_profiles(id),
    delegator_profile_id uuid not null references delegator_profiles(id),
    delegated_at timestamp not null,
    revoked_at timestamp,
    reason varchar(500),
    created_at timestamp not null,
    updated_at timestamp not null
);

create table audit_logs (
    id uuid primary key,
    actor_user_id uuid references users(id),
    action varchar(120) not null,
    target_type varchar(120),
    target_id uuid,
    details varchar(2000),
    created_at timestamp not null,
    updated_at timestamp not null
);