package com.jaqqen.tapeshub.exception;

public class TapeAlreadyExistsException extends RuntimeException {

    public TapeAlreadyExistsException(String id) {
        super("A tape with id '" + id + "' already exists");
    }
}
