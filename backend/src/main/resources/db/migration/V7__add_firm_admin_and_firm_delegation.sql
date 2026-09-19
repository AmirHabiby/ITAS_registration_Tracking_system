ALTER TABLE representative_profiles
    ADD COLUMN firm_id uuid REFERENCES firms(id);

CREATE INDEX idx_representative_firm ON representative_profiles(firm_id);

CREATE TABLE firm_admin_profiles (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL UNIQUE REFERENCES users(id),
    firm_id uuid NOT NULL UNIQUE REFERENCES firms(id),
    full_name varchar(160) NOT NULL,
    email varchar(160) NOT NULL UNIQUE,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE INDEX idx_firm_admin_firm ON firm_admin_profiles(firm_id);

CREATE TABLE firm_delegations (
    id uuid PRIMARY KEY,
    firm_id uuid NOT NULL REFERENCES firms(id),
    delegator_profile_id uuid NOT NULL REFERENCES delegator_profiles(id),
    delegated_at timestamp NOT NULL,
    revoked_at timestamp,
    reason varchar(500),
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE INDEX idx_firm_delegation_firm ON firm_delegations(firm_id);
CREATE INDEX idx_firm_delegation_delegator ON firm_delegations(delegator_profile_id);

CREATE TABLE firm_agent_assignments (
    id uuid PRIMARY KEY,
    firm_delegation_id uuid NOT NULL REFERENCES firm_delegations(id),
    representative_profile_id uuid NOT NULL REFERENCES representative_profiles(id),
    assigned_by_user_id uuid NOT NULL REFERENCES users(id),
    assigned_at timestamp NOT NULL,
    revoked_at timestamp,
    reason varchar(500),
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL
);

CREATE INDEX idx_firm_assignment_delegation ON firm_agent_assignments(firm_delegation_id);
CREATE INDEX idx_firm_assignment_representative ON firm_agent_assignments(representative_profile_id);
CREATE UNIQUE INDEX uq_active_firm_assignment
    ON firm_agent_assignments(representative_profile_id)
    WHERE revoked_at IS NULL;