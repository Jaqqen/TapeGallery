package com.jaqqen.tapeshub.genre;

import org.jmolecules.ddd.types.Identifier;

import java.util.UUID;

/**
 * A genre's identity: minted once, never changed.
 */
public record GenreId(UUID value) implements Identifier {

    public static GenreId newId() {
        return new GenreId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
