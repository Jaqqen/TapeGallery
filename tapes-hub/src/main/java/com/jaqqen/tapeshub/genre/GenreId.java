package com.jaqqen.tapeshub.genre;

import com.jaqqen.tapeshub.shared.InvalidIdentifierException;
import org.jmolecules.ddd.types.Identifier;

import java.util.UUID;

/**
 * A genre's identity - minted once, never changed.
 *
 * <p>Published deliberately: it is the handle other modules hold instead of a {@code Genre}, which
 * keeps aggregates referring to each other by id rather than by object graph.
 */
public record GenreId(UUID value) implements Identifier {

    public GenreId {
        if (value == null) {
            throw new InvalidIdentifierException("genre id");
        }
    }

    public static GenreId newId() {
        return new GenreId(UUID.randomUUID());
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String toString() {
        return value.toString();
    }
}
