CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    google_sub  VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL
);

CREATE TABLE players (
    id              BIGSERIAL PRIMARY KEY,
    display_name    VARCHAR(255) NOT NULL,
    melee_username  VARCHAR(255) NOT NULL UNIQUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE seasons (
    id          BIGSERIAL PRIMARY KEY,
    year        INTEGER      NOT NULL UNIQUE,
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE tournaments (
    id           BIGSERIAL PRIMARY KEY,
    melee_id     VARCHAR(255) NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    date         DATE         NOT NULL,
    store_name   VARCHAR(255) NOT NULL,
    season_id    BIGINT       NOT NULL REFERENCES seasons(id),
    organizer_id BIGINT       NOT NULL REFERENCES users(id),
    status       VARCHAR(50)  NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE archetypes (
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE tournament_results (
    id              BIGSERIAL PRIMARY KEY,
    tournament_id   BIGINT       NOT NULL REFERENCES tournaments(id),
    player_id       BIGINT       REFERENCES players(id),
    archetype_id    BIGINT       REFERENCES archetypes(id),
    position        INTEGER      NOT NULL,
    points          INTEGER      NOT NULL,
    melee_username  VARCHAR(255) NOT NULL,
    decklist_url    VARCHAR(500) NOT NULL DEFAULT '',
    status          VARCHAR(50)  NOT NULL
);

CREATE INDEX idx_results_player_season
    ON tournament_results(player_id);
CREATE INDEX idx_results_tournament
    ON tournament_results(tournament_id);
