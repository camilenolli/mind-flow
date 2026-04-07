-- MindFlow — schema PostgreSQL (referência; em runtime o JPA cria via ddl-auto=update)

CREATE TABLE IF NOT EXISTS focus_profile (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(80) NOT NULL,
    start_time  TIME,
    end_time    TIME
);

CREATE TABLE IF NOT EXISTS tag (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS note (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(200) NOT NULL,
    content           TEXT NOT NULL,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL,
    focus_profile_id  BIGINT REFERENCES focus_profile(id) ON DELETE SET NULL
);

-- Tabela associativa N:N — coração do "conceitos relacionados"
CREATE TABLE IF NOT EXISTS note_tag (
    note_id BIGINT NOT NULL REFERENCES note(id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tag(id)  ON DELETE CASCADE,
    PRIMARY KEY (note_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_note_tag_tag ON note_tag(tag_id);

CREATE TABLE IF NOT EXISTS flashcard (
    id                BIGSERIAL PRIMARY KEY,
    question          VARCHAR(500) NOT NULL,
    answer            TEXT NOT NULL,
    created_at        TIMESTAMP NOT NULL,
    note_id           BIGINT REFERENCES note(id) ON DELETE SET NULL,
    focus_profile_id  BIGINT REFERENCES focus_profile(id) ON DELETE SET NULL
);
