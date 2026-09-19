package com.jaqqen.tapeshub.tape.domain;

/** Unchecked exception: a missing tape is a 404, not something a caller can recover from. */
public class TapeNotFoundException extends RuntimeException {

    public TapeNotFoundException(TapeId id) {
        super("No tape with id '%s'".formatted(id));
    }
}
