package com.jaqqen.tapeshub.exception;

public class TapeNotFoundException extends RuntimeException {

    public TapeNotFoundException(String id) {
        super("No tape with id '" + id + "'");
    }
}
