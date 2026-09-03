package com.jaqqen.tapeshub.tape.domain;

import com.jaqqen.tapeshub.shared.InvalidIdentifierException;
import org.jmolecules.ddd.types.Identifier;

import java.util.UUID;

/**
 * A tape's identity: minted once, never changed, and what the API's URLs carry. It is the only
 * handle on a tape - a title is free to change and free to repeat.
 */
public record TapeId(UUID value) implements Identifier {

    public TapeId {
        if (value == null) {
            throw new InvalidIdentifierException("tape id");
        }
    }

    public static TapeId newId() {
        return new TapeId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
