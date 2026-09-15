-- Reference data: the genreService a tape can belong to. Owned by the genre module alone - the tape
-- module reaches it only through the genre module's published API, never through this table.
--
-- created_at / modified_at / deleted_at are on every table here: when a row was written, when it
-- last changed, and when it was deleted.
--
-- deleted_at is what turns DELETE into a soft delete. The row and its foreign keys survive; the
-- repositories filter on deleted_at IS NULL, so a deleted tape or genre is invisible to the API
-- while the fact that it existed, and when it went, is not thrown away.
--
-- timestamptz, not timestamp: these are instants, unlike tape.release_date which is a calendar day.
--
-- The DEFAULT is for hand-written SQL - the dev seed in db/seed, a fixture, a manual insert. The
-- application never leans on it: Lifecycle stamps both columns before the row is written.
CREATE TABLE genre
(
    id          UUID        NOT NULL,
    name        VARCHAR(64) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ,
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
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    modified_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at   TIMESTAMPTZ,
    CONSTRAINT pk_tape PRIMARY KEY (id),
    CONSTRAINT fk_tape_genre FOREIGN KEY (genre_id) REFERENCES genre (id)
);

COMMENT ON COLUMN tape.id IS 'Identity: minted once, never changes, and what the API URLs carry';
COMMENT ON COLUMN tape.duration IS 'Runtime in milliseconds';
COMMENT ON COLUMN tape.pattern IS 'Kebab-case wire value of TapePattern, e.g. retro-blocks';
COMMENT ON COLUMN genre.deleted_at IS 'Soft delete: NULL while live, set once when deleted';
COMMENT ON COLUMN tape.deleted_at IS 'Soft delete: NULL while live, set once when deleted';

-- Every read carries "deleted_at IS NULL", so the live rows are worth indexing on their own.
-- Partial indexes: deleted rows are dead weight in an index nothing queries them through.
CREATE INDEX idx_genre_live ON genre (name) WHERE deleted_at IS NULL;
CREATE INDEX idx_tape_live ON tape (title) WHERE deleted_at IS NULL;

-- Backs the "is this genre still in use?" check that replaces fk_tape_genre's implicit veto: once a
-- delete stops removing the row, the foreign key has nothing left to refuse.
CREATE INDEX idx_tape_live_genre ON tape (genre_id) WHERE deleted_at IS NULL;
