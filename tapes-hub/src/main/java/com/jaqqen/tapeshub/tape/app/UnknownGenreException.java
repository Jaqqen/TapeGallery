package com.jaqqen.tapeshub.tape.app;

import com.jaqqen.tapeshub.genre.GenreId;

/**
 * A write named a genre that does not exist. Raised here rather than in the domain because only the
 * application ring may ask the genre module whether an id resolves.
 */
public class UnknownGenreException extends RuntimeException {

    public UnknownGenreException(GenreId id) {
        super("No genre with id '%s'".formatted(id));
    }
}
