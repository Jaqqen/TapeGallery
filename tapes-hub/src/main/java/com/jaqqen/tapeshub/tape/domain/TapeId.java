package com.jaqqen.tapeshub.tape.domain;

import org.jmolecules.ddd.types.Identifier;

import java.util.UUID;

/**
 * A tape's identity: minted once, never changed. This is the only handle on a tape.
 */
public record TapeId(UUID value) implements Identifier {

    public static TapeId newId() {
        return new TapeId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
