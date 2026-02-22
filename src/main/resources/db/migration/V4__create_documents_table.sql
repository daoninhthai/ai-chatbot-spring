CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE documents (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name     VARCHAR(500) NOT NULL,
    content_type  VARCHAR(100),
    file_size     BIGINT,
    chunk_count   INTEGER,
    status        VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    error_message TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_documents_status ON documents(status);
