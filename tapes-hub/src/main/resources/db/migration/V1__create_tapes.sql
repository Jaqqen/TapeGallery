-- Reference data: the genreService a tape can belong to. Owned by the genre module alone - the tape
-- module reaches it only through the genre module's published API, never through this table.
CREATE TABLE genre
(
    id          UUID        NOT NULL,
    name        VARCHAR(64) NOT NULL,
    description TEXT,
    CONSTRAINT pk_genre PRIMARY KEY (id)
);

-- The tapes catalogue.
--
-- id is the identity: minted once, never changed, and what the API's URLs carry.
-- It is the only handle on a tape - a title is free to change and free to repeat.
--
-- The four sleeve colours live on this row rather than in a table of their own: they have no
-- identity and no lifecycle apart from the tape, so they are a value object, not an entity.
-- The first colour is "central" rather than "primary": the latter is a reserved SQL keyword,
-- which forces quoting everywhere and trips up linters and static analysis.
--
-- genre_id is a plain foreign key, not a mapped association: it is how one aggregate refers to
-- another. The constraint is what makes "a tape cannot exist without a genre" true in storage.
CREATE TABLE tape
(
    id           UUID         NOT NULL,
    title        VARCHAR(255) NOT NULL,
    subtitle     VARCHAR(255),
    release_date DATE         NOT NULL,
    genre_id     UUID         NOT NULL,
    duration     INTEGER      NOT NULL,
    central      VARCHAR(9)   NOT NULL,
    secondary    VARCHAR(9)   NOT NULL,
    accent       VARCHAR(9)   NOT NULL,
    label        VARCHAR(9)   NOT NULL,
    pattern      VARCHAR(32)  NOT NULL,
    CONSTRAINT pk_tape PRIMARY KEY (id),
    CONSTRAINT fk_tape_genre FOREIGN KEY (genre_id) REFERENCES genre (id)
);

COMMENT ON COLUMN tape.id IS 'Identity: minted once, never changes, and what the API URLs carry';
COMMENT ON COLUMN tape.duration IS 'Runtime in milliseconds';
COMMENT ON COLUMN tape.pattern IS 'Kebab-case wire value of TapePattern, e.g. retro-blocks';
