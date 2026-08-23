package com.jaqqen.tapeshub.exception;

import java.util.UUID;

public class TapeNotFoundException extends RuntimeException {

    public TapeNotFoundException(UUID id) {
        super("No tape with id '" + id + "'");
    }
}
