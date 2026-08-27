-- Reference data: the genres a tape can belong to. Looked up by id, never
-- owned or written by the tape aggregate (see TapeGenreRepository).
CREATE TABLE genre
(
    id          UUID        NOT NULL,
    name        VARCHAR(64) NOT NULL,
    description TEXT,
    CONSTRAINT pk_genre PRIMARY KEY (id)
);

-- The four hex colours a tape's sleeve is rendered with. One row per tape;
-- "primary" is a reserved SQL keyword, so it must stay quoted everywhere.
CREATE TABLE tape_colors
(
    id          UUID       NOT NULL,
    "primary"   VARCHAR(9) NOT NULL,
    secondary   VARCHAR(9) NOT NULL,
    accent      VARCHAR(9) NOT NULL,
    label       VARCHAR(9) NOT NULL,
    CONSTRAINT pk_tape_colors PRIMARY KEY (id)
);

-- The tapes catalogue.
--
-- id is the identity: minted once, never changed, and what the API's URLs carry.
-- It is the only handle on a tape - a title is free to change and free to repeat.
CREATE TABLE tape
(
    id           UUID         NOT NULL,
    title        VARCHAR(255) NOT NULL,
    subtitle     VARCHAR(255),
    release_date DATE         NOT NULL,
    genre_id     UUID         NOT NULL,
    duration     INTEGER      NOT NULL,
    colors_id    UUID         NOT NULL,
    pattern      VARCHAR(32)  NOT NULL,
    CONSTRAINT pk_tape PRIMARY KEY (id),
    CONSTRAINT fk_tape_genre FOREIGN KEY (genre_id) REFERENCES genre (id),
    CONSTRAINT fk_tape_colors FOREIGN KEY (colors_id) REFERENCES tape_colors (id)
);

COMMENT ON COLUMN tape.id IS 'Identity: minted once, never changes, and what the API URLs carry';
COMMENT ON COLUMN tape.duration IS 'Runtime in milliseconds';
COMMENT ON COLUMN tape.pattern IS 'Kebab-case wire value of TapePattern, e.g. retro-blocks';
