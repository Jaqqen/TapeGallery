package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;

/** Something still points at this genre, so it cannot be deleted. */
public class GenreInUseException extends RuntimeException {

    public GenreInUseException(GenreId id) {
        super("Genre '%s' is still in use and cannot be deleted".formatted(id));
    }
}
