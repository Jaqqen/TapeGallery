package com.jaqqen.tapeshub.tape.domain.exception;

import com.jaqqen.tapeshub.tape.domain.TapeId;

import java.util.UUID;

public class TapeNotFoundException extends Exception {
    private final TapeId tapeId;
    public TapeNotFoundException(UUID id) {
        this.tapeId = new TapeId(id);
    }

    @Override
    public String getMessage() {
        return "Tape with id %s not found".formatted(tapeId.id());
    }
}
