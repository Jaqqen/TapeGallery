package com.jaqqen.tapeshub.shared;

/**
 * An identity value object - {@code TapeId}, {@code GenreId} - was constructed without a value.
 * Unchecked, and distinct from the {@link IllegalArgumentException} other value objects throw for
 * invalid content, so a missing identifier can be told apart from a malformed one.
 */
public class InvalidIdentifierException extends RuntimeException {

    public InvalidIdentifierException(String what) {
        super("%s must not be null".formatted(what));
    }
}
