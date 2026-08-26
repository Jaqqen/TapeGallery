package com.jaqqen.tapeshub.tape.domain;

import org.springframework.util.Assert;

import java.util.UUID;

public record TapeId(UUID id) {
    public TapeId {
        Assert.notNull(id, "Tape ID must not be null");
        Assert.isInstanceOf(UUID.class, id, "Tape ID must be of type UUID");
    }

    public TapeId() {
        this(UUID.randomUUID());
    }
}
