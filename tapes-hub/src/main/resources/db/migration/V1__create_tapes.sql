-- The tapes catalogue.
--
-- id is a surrogate key that never changes. The public identifier is the slug,
-- which TapeService derives from the title, so it moves when a tape is retitled;
-- anything that references a tape from elsewhere must key off id instead.
CREATE TABLE tapes
(
    id              UUID         NOT NULL,
    slug            VARCHAR(255) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    subtitle        VARCHAR(255),
    year            VARCHAR(32)  NOT NULL,
    genre           VARCHAR(64)  NOT NULL,
    duration        VARCHAR(32)  NOT NULL,
    rating          VARCHAR(32)  NOT NULL,
    description     TEXT         NOT NULL,
    color_primary   VARCHAR(9)   NOT NULL,
    color_secondary VARCHAR(9)   NOT NULL,
    color_accent    VARCHAR(9)   NOT NULL,
    color_label     VARCHAR(9)   NOT NULL,
    pattern         VARCHAR(32)  NOT NULL,
    CONSTRAINT pk_tapes PRIMARY KEY (id),
    CONSTRAINT uq_tapes_slug UNIQUE (slug)
);

-- year and rating are free-form strings in the web-portal's Tape interface and in
-- the Tape record, so they stay strings here rather than being parsed into numbers.

COMMENT ON COLUMN tapes.pattern IS 'Kebab-case wire value of TapePattern, e.g. retro-blocks';
