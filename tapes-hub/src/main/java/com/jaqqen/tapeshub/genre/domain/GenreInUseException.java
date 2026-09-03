package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;

/**
 * Something still points at this genre, so it cannot be deleted.
 *
 * <p>Deliberately vague about who: the genre module does not know that tapes exist. The foreign key
 * is what enforces the rule, and this is the translation of that refusal into a domain term.
 */
public class GenreInUseException extends RuntimeException {

    public GenreInUseException(GenreId id) {
        super("Genre '%s' is still in use and cannot be deleted".formatted(id));
    }
}
