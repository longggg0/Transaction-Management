ALTER TABLE users ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE users SET enabled = TRUE;   -- existing test users stay usable

CREATE TABLE otp_codes (
                           id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           user_id    VARCHAR(50)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           code_hash  VARCHAR(200) NOT NULL,
                           purpose    VARCHAR(20)  NOT NULL,
                           expires_at TIMESTAMP    NOT NULL,
                           attempts   INTEGER      NOT NULL DEFAULT 0,
                           used       BOOLEAN      NOT NULL DEFAULT FALSE,
                           created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_otp_user_purpose ON otp_codes (user_id, purpose, created_at DESC);