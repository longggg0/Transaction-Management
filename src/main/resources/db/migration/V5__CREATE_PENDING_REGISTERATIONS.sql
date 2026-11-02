CREATE TABLE pending_registrations (
                                       id            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       email         VARCHAR(200) NOT NULL UNIQUE,
                                       username      VARCHAR(50)  NOT NULL,
                                       password_hash VARCHAR(200) NOT NULL,
                                       code_hash     VARCHAR(200) NOT NULL,
                                       attempts      INTEGER      NOT NULL DEFAULT 0,
                                       expires_at    TIMESTAMP    NOT NULL,
                                       sent_at       TIMESTAMP    NOT NULL,
                                       created_at    TIMESTAMP    NOT NULL DEFAULT now()
);