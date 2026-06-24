CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    url_avatar TEXT,
    role       VARCHAR(25)  NOT NULL,
    state      VARCHAR(25)  NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'TOURIST')),
    CONSTRAINT chk_user_state CHECK (state IN ('ACTIVE', 'INACTIVE', 'BANNED')),
    CONSTRAINT unq_user_email UNIQUE (email)
);

CREATE TABLE destination_types
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unq_destination_type_name UNIQUE (name)
);

CREATE TABLE destinations
(
    id                  UUID PRIMARY KEY,
    name                VARCHAR(255)   NOT NULL,
    description         TEXT           NOT NULL,
    country             VARCHAR(100)   NOT NULL,
    city                VARCHAR(100)   NOT NULL,
    latitude            DECIMAL(10, 2) NOT NULL,
    longitude           DECIMAL(10, 2) NOT NULL,
    destination_type_id UUID           NOT NULL,
    url_banner          TEXT,
    state               VARCHAR(25)    NOT NULL DEFAULT 'DRAFT',
    created_at          TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unq_destination_name UNIQUE (name),
    CONSTRAINT chk_destination_state CHECK (state IN ('ACTIVE', 'INACTIVE', 'DRAFT')),
    CONSTRAINT fk_destination_type FOREIGN KEY (destination_type_id) REFERENCES destination_types (id) ON DELETE RESTRICT
);

CREATE TABLE destination_images
(
    id             UUID PRIMARY KEY,
    destination_id UUID NOT NULL,
    url_image      TEXT NOT NULL,
    CONSTRAINT fk_destination_image FOREIGN KEY (destination_id) REFERENCES destinations (id) ON DELETE CASCADE
);

CREATE TABLE destination_reviews
(
    id             UUID PRIMARY KEY,
    destination_id UUID NOT NULL,
    user_id        UUID NOT NULL,
    value          INT  NOT NULL,
    content        TEXT NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_review_value CHECK (value BETWEEN 1 AND 5),
    CONSTRAINT unq_review_destination_user UNIQUE (destination_id, user_id),
    CONSTRAINT fk_review_destination FOREIGN KEY (destination_id) REFERENCES destinations (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE destination_likes
(
    id             UUID PRIMARY KEY,
    destination_id UUID NOT NULL,
    user_id        UUID NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unq_like_destination_user UNIQUE (destination_id, user_id),
    CONSTRAINT fk_like_destination FOREIGN KEY (destination_id) REFERENCES destinations (id) ON DELETE CASCADE,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE destination_saves
(
    id             UUID PRIMARY KEY,
    destination_id UUID NOT NULL,
    user_id        UUID NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unq_save_destination_user UNIQUE (destination_id, user_id),
    CONSTRAINT fk_save_destination FOREIGN KEY (destination_id) REFERENCES destinations (id) ON DELETE CASCADE,
    CONSTRAINT fk_save_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY,
    token      VARCHAR(255) NOT NULL,
    user_id    UUID         NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unq_refresh_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_destination_images_destination_id ON destination_images (destination_id);

CREATE INDEX idx_destination_reviews_user_id ON destination_reviews (user_id);
CREATE INDEX idx_destination_likes_user_id ON destination_likes (user_id);
CREATE INDEX idx_destination_saves_user_id ON destination_saves (user_id);

CREATE INDEX idx_destinations_destination_type_id ON destinations (destination_type_id);
CREATE INDEX idx_destinations_state ON destinations (state);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);