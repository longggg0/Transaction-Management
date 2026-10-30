CREATE TABLE users (
                       id         VARCHAR(50)  PRIMARY KEY,
                       username   VARCHAR(50)  NOT NULL UNIQUE,
                       password   VARCHAR(200) NOT NULL,
                       email      VARCHAR(200) NOT NULL UNIQUE,
                       created_at TIMESTAMP    NOT NULL DEFAULT now(),
                       CONSTRAINT chk_user_email_lower CHECK (email = lower(email))
);

CREATE TABLE categories (
                            id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            user_id    VARCHAR(50)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            name       VARCHAR(50)  NOT NULL,
                            created_at TIMESTAMP    NOT NULL DEFAULT now(),
                            CONSTRAINT unq_categories_user_name UNIQUE (user_id, name)
);

CREATE TABLE transactions (
                              id          VARCHAR(50)   PRIMARY KEY,
                              user_id     VARCHAR(50)   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              category_id INTEGER       REFERENCES categories(id) ON DELETE SET NULL,
                              amount      NUMERIC(14,2) NOT NULL CHECK (amount > 0),
                              currency    VARCHAR(3)    NOT NULL DEFAULT 'USD',
                              receiver    VARCHAR(100)  NOT NULL,
                              reason      VARCHAR(255)  NOT NULL,
                              paid_at     TIMESTAMP     NOT NULL DEFAULT now(),
                              created_at  TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_user_paid_at ON transactions (user_id, paid_at DESC);
CREATE INDEX idx_transactions_category     ON transactions (category_id);