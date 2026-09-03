package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;

/** Unchecked exception: a missing genre is a 404, not something a caller can recover from. */
public class GenreNotFoundException extends RuntimeException {

    public GenreNotFoundException(GenreId id) {
        super("No genre with id '%s'".formatted(id));
    }
}
