DROP TABLE bot_session_data;
DROP TABLE bot_users;
DROP TABLE bot_activation_tokens;

CREATE TABLE bot_activation_tokens (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    token UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,

    CONSTRAINT fk_bot_activation_tokens_users
            FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT uq_bot_activation_tokens_token
        UNIQUE (token)
);

CREATE TABLE bot_users (
    id SERIAL PRIMARY KEY,
    user_id INT NULL,
    platform VARCHAR(250),
    provider_user_id VARCHAR(500),
    current_state VARCHAR(1000),
    previous_state VARCHAR(1000),

    CONSTRAINT fk_bot_users_users
                FOREIGN KEY (user_id)
                REFERENCES users(id)
                ON DELETE CASCADE,

    CONSTRAINT uq_bot_users_platform_provider_user_id
            UNIQUE (platform, provider_user_id)
);

CREATE TABLE bot_session_data (
    id SERIAL PRIMARY KEY,
    bot_user_id INT NOT NULL,
    state VARCHAR(100) NOT NULL,
    key VARCHAR(100) NOT NULL,
    value JSONB NOT NULL,

    CONSTRAINT fk_bot_session_data_bot_users
                FOREIGN KEY (bot_user_id)
                REFERENCES bot_users(id)
                ON DELETE CASCADE,

    CONSTRAINT uq_bot_session_data_bot_user_id_key
        UNIQUE (bot_user_id, key)
);