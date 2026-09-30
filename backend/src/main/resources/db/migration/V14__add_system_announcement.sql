CREATE TABLE system_announcement (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    announcement_text TEXT NOT NULL
);
