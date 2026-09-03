package com.jaqqen.tapeshub.genre.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

/** A genre's name. Unique enough to look one up by, but free to change - the id is the identity. */
@ValueObject
public record GenreName(String value) {

    /** Mirrors {@code genre.name VARCHAR(64)} in V1__create_tapes.sql. */
    private static final int MAX_LENGTH = 64;

    public GenreName {
        Assert.hasText(value, "genre name must not be blank");
        Assert.isTrue(value.length() <= MAX_LENGTH,
            () -> "genre name must be at most %d characters".formatted(MAX_LENGTH));
    }

    @Override
    public String toString() {
        return value;
    }
}
