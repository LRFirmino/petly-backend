-- =========================================================================
CREATE TABLE petshops (
    id               integer PRIMARY KEY AUTOINCREMENT,
    name             varchar(160) NOT NULL,
    slug             varchar(160) NOT NULL,
    email            varchar(160),
    phone            varchar(40),
    address          varchar(240),
    cnpj             varchar(20),
    logo_url         text,
    platform_account boolean      NOT NULL DEFAULT 0,
    active           boolean      NOT NULL DEFAULT 1,
    created_at       timestamp    NOT NULL,
    CONSTRAINT uk_petshops_slug UNIQUE (slug)
);

CREATE TABLE users (
    id            integer PRIMARY KEY AUTOINCREMENT,
    petshop_id    integer      NOT NULL,
    name          varchar(160) NOT NULL,
    email         varchar(160) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role          varchar(32)  NOT NULL,
    active        boolean      NOT NULL DEFAULT 1,
    created_at    timestamp    NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_petshop FOREIGN KEY (petshop_id) REFERENCES petshops (id)
);
CREATE INDEX idx_users_petshop ON users (petshop_id);

CREATE TABLE clients (
    id         integer PRIMARY KEY AUTOINCREMENT,
    petshop_id integer       NOT NULL,
    name       varchar(160)  NOT NULL,
    email      varchar(160),
    phone      varchar(40),
    notes      varchar(1000),
    created_at timestamp     NOT NULL,
    CONSTRAINT fk_clients_petshop FOREIGN KEY (petshop_id) REFERENCES petshops (id)
);
CREATE INDEX idx_clients_petshop ON clients (petshop_id);

CREATE TABLE pets (
    id         integer PRIMARY KEY AUTOINCREMENT,
    petshop_id integer      NOT NULL,
    client_id  integer      NOT NULL,
    name       varchar(120) NOT NULL,
    species    varchar(80),
    breed      varchar(120),
    size_tier  varchar(32),
    birth_date date,
    notes      varchar(1000),
    created_at timestamp    NOT NULL,
    CONSTRAINT fk_pets_petshop FOREIGN KEY (petshop_id) REFERENCES petshops (id),
    CONSTRAINT fk_pets_client FOREIGN KEY (client_id) REFERENCES clients (id)
);
CREATE INDEX idx_pets_petshop ON pets (petshop_id);
CREATE INDEX idx_pets_client ON pets (client_id);

CREATE TABLE services (
    id               integer PRIMARY KEY AUTOINCREMENT,
    petshop_id       integer      NOT NULL,
    name             varchar(160) NOT NULL,
    description      varchar(500),
    duration_minutes integer,
    price_small      decimal(10, 2),
    price_medium     decimal(10, 2),
    price_large      decimal(10, 2),
    created_at       timestamp    NOT NULL,
    CONSTRAINT fk_services_petshop FOREIGN KEY (petshop_id) REFERENCES petshops (id)
);
CREATE INDEX idx_services_petshop ON services (petshop_id);

CREATE TABLE combos (
    id          integer PRIMARY KEY AUTOINCREMENT,
    petshop_id  integer      NOT NULL,
    name        varchar(160) NOT NULL,
    description varchar(500),
    price_from  decimal(10, 2),
    popularity  integer,
    created_at  timestamp    NOT NULL,
    CONSTRAINT fk_combos_petshop FOREIGN KEY (petshop_id) REFERENCES petshops (id)
);
CREATE INDEX idx_combos_petshop ON combos (petshop_id);

CREATE TABLE team_members (
    id             integer PRIMARY KEY AUTOINCREMENT,
    petshop_id     integer      NOT NULL,
    name           varchar(160) NOT NULL,
    role           varchar(120),
    specialty      varchar(160),
    rating         varchar(10),
    daily_capacity integer,
    active         boolean      NOT NULL DEFAULT 1,
    created_at     timestamp    NOT NULL,
    CONSTRAINT fk_team_petshop FOREIGN KEY (petshop_id) REFERENCES petshops (id)
);
CREATE INDEX idx_team_petshop ON team_members (petshop_id);

CREATE TABLE appointments (
    id               integer PRIMARY KEY AUTOINCREMENT,
    petshop_id       integer      NOT NULL,
    client_id        integer      NOT NULL,
    pet_id           integer,
    service          varchar(160) NOT NULL,
    scheduled_at     timestamp    NOT NULL,
    duration_minutes integer,
    price            decimal(10, 2),
    status           varchar(32)  NOT NULL,
    staff_name       varchar(160),
    notes            varchar(1000),
    created_at       timestamp    NOT NULL,
    CONSTRAINT fk_appointments_petshop FOREIGN KEY (petshop_id) REFERENCES petshops (id),
    CONSTRAINT fk_appointments_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_appointments_pet FOREIGN KEY (pet_id) REFERENCES pets (id)
);
CREATE INDEX idx_appointments_petshop ON appointments (petshop_id);
CREATE INDEX idx_appointments_client ON appointments (client_id);
CREATE INDEX idx_appointments_pet ON appointments (pet_id);
CREATE INDEX idx_appointments_staff ON appointments (petshop_id, staff_name);
