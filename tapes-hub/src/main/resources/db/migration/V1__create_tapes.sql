-- The tapes catalogue.
--
-- id is the identity: minted once, never changed, and what the API's URLs carry.
-- It is the only handle on a tape - a title is free to change and free to repeat.
CREATE TABLE tapes
(
    id              UUID         NOT NULL,
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
    CONSTRAINT pk_tapes PRIMARY KEY (id)
);

-- year and rating are free-form strings in the web-portal's Tape interface and in
-- the Tape record, so they stay strings here rather than being parsed into numbers.

COMMENT ON COLUMN tapes.id IS 'Identity: minted once, never changes, and what the API URLs carry';
COMMENT ON COLUMN tapes.pattern IS 'Kebab-case wire value of TapePattern, e.g. retro-blocks';
