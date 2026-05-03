CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users
(
    id             uuid PRIMARY KEY      DEFAULT gen_random_uuid(),
    email          varchar(255) NOT NULL,
    password_hash  varchar(255) NOT NULL,
    email_verified boolean      NOT NULL DEFAULT false,
    status         varchar(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    version        bigint       NOT NULL DEFAULT 0,
    CONSTRAINT users_email_uk UNIQUE (email),
    CONSTRAINT users_status_chk CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED')),
    CONSTRAINT users_email_lower_chk CHECK (email = lower(email))
);

CREATE TABLE IF NOT EXISTS user_profiles
(
    user_id      uuid PRIMARY KEY,
    display_name varchar(100) NOT NULL,
    avatar_url   varchar(500),
    location     varchar(200),
    bio          text,
    updated_at   timestamptz  NOT NULL DEFAULT now(),
    version      bigint       NOT NULL DEFAULT 0,
    CONSTRAINT user_profiles_user_fk FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id         uuid PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id    uuid         NOT NULL,
    token_hash varchar(255) NOT NULL,
    expires_at timestamptz  NOT NULL,
    revoked    boolean      NOT NULL DEFAULT false,
    created_at timestamptz  NOT NULL DEFAULT now(),
    version    bigint       NOT NULL DEFAULT 0,
    CONSTRAINT refresh_tokens_user_fk FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS auth_tokens
(
    id         uuid PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id    uuid         NOT NULL,
    token_hash varchar(255) NOT NULL,
    type       varchar(32)  NOT NULL,
    expires_at timestamptz  NOT NULL,
    used_at    timestamptz,
    created_at timestamptz  NOT NULL DEFAULT now(),
    version    bigint       NOT NULL DEFAULT 0,
    CONSTRAINT auth_tokens_user_fk FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT auth_tokens_type_chk CHECK (type IN ('EMAIL_VERIFY', 'PASSWORD_RESET'))
);

CREATE TABLE IF NOT EXISTS categories
(
    id        uuid PRIMARY KEY      DEFAULT gen_random_uuid(),
    parent_id uuid,
    name      varchar(100) NOT NULL,
    slug      varchar(100) NOT NULL,
    version   bigint       NOT NULL DEFAULT 0,
    CONSTRAINT categories_slug_uk UNIQUE (slug),
    CONSTRAINT categories_parent_fk FOREIGN KEY (parent_id) REFERENCES categories (id)
);

CREATE TABLE IF NOT EXISTS listings
(
    id            uuid PRIMARY KEY        DEFAULT gen_random_uuid(),
    seller_id     uuid           NOT NULL,
    category_id   uuid           NOT NULL,
    title         varchar(200)   NOT NULL,
    description   text,
    price         numeric(12, 2) NOT NULL,
    currency      varchar(3)     NOT NULL DEFAULT 'MDL',
    condition     varchar(16)    NOT NULL,
    status        varchar(16)    NOT NULL DEFAULT 'DRAFT',
    location      varchar(200),
    attributes    jsonb          NOT NULL DEFAULT '{}'::jsonb,
    search_vector tsvector,
    created_at    timestamptz    NOT NULL DEFAULT now(),
    updated_at    timestamptz    NOT NULL DEFAULT now(),
    version       bigint         NOT NULL DEFAULT 0,
    CONSTRAINT listings_seller_fk FOREIGN KEY (seller_id) REFERENCES users (id),
    CONSTRAINT listings_category_fk FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT listings_condition_chk CHECK (condition IN ('NEW', 'LIKE_NEW', 'GOOD', 'FAIR', 'POOR')),
    CONSTRAINT listings_status_chk CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED', 'DELETED')),
    CONSTRAINT listings_currency_chk CHECK (char_length(currency) = 3)
);

CREATE TABLE IF NOT EXISTS listing_photos
(
    id            uuid PRIMARY KEY      DEFAULT gen_random_uuid(),
    listing_id    uuid         NOT NULL,
    url           varchar(500) NOT NULL,
    display_order smallint     NOT NULL DEFAULT 0,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    version       bigint       NOT NULL DEFAULT 0,
    CONSTRAINT listing_photos_listing_fk FOREIGN KEY (listing_id) REFERENCES listings (id),
    CONSTRAINT listing_photos_order_chk CHECK (display_order >= 0),
    CONSTRAINT listing_photos_listing_order_uk UNIQUE (listing_id, display_order)
);

CREATE INDEX IF NOT EXISTS refresh_tokens_user_revoked_idx ON refresh_tokens (user_id, revoked);
CREATE INDEX IF NOT EXISTS auth_tokens_user_type_used_idx ON auth_tokens (user_id, type, used_at);
CREATE INDEX IF NOT EXISTS categories_parent_idx ON categories (parent_id);
CREATE INDEX IF NOT EXISTS listings_search_vector_idx ON listings USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS listings_attributes_idx ON listings USING GIN (attributes);
CREATE INDEX IF NOT EXISTS listings_seller_status_idx ON listings (seller_id, status);
CREATE INDEX IF NOT EXISTS listings_category_status_idx ON listings (category_id, status);
CREATE INDEX IF NOT EXISTS listing_photos_listing_idx ON listing_photos (listing_id);

CREATE OR REPLACE FUNCTION listings_search_vector_trigger_fn()
    RETURNS trigger AS
$$
BEGIN
    NEW.search_vector :=
            setweight(to_tsvector('simple', coalesce(NEW.title, '')), 'A') ||
            setweight(to_tsvector('simple', coalesce(NEW.description, '')), 'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER listings_search_vector_trigger
    BEFORE INSERT OR UPDATE OF title, description
    ON listings
    FOR EACH ROW
EXECUTE FUNCTION listings_search_vector_trigger_fn();

