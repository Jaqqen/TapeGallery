package com.jaqqen.tapeshub.tape.app.exception;

import java.util.UUID;

public class TapeNotFoundRuntimeException extends RuntimeException {

    public TapeNotFoundRuntimeException(UUID id) {
        super("No tape with id '" + id + "'");
    }
}
