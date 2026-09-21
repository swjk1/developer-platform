-- Technologies are a separate table rather than a delimited column, because
-- section 18 wants them searchable and a future "all projects using Kotlin"
-- filter should be an index lookup, not a LIKE over a comma-separated string.

CREATE TABLE project_technologies (
    project_id    BIGINT       NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    technology    VARCHAR(80)  NOT NULL,
    display_order INTEGER      NOT NULL DEFAULT 0,

    CONSTRAINT pk_project_technologies PRIMARY KEY (project_id, technology)
);

CREATE INDEX idx_project_technologies_technology ON project_technologies (technology);
