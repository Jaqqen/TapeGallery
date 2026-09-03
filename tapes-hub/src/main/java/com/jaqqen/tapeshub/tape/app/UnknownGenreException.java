package com.jaqqen.tapeshub.tape.app;

import com.jaqqen.tapeshub.genre.GenreId;

/**
 * Declares that the Genre with the provided id does not exist.
 * <p>
 *     Raised here rather than in the domain because only the application ring may ask the genre
 *     module whether an id resolves.
 * </p>
 */
public class UnknownGenreException extends RuntimeException {

    public UnknownGenreException(GenreId id) {
        super("No genre with id '%s'".formatted(id));
    }
}
