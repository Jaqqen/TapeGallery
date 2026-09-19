package com.jaqqen.tapeshub.tape.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TapeNotFoundExceptionTest {

    @Test
    void namesTheTapeItLookedFor() {
        TapeId id = new TapeId(UUID.fromString("3f2504e0-4f89-41d3-9a0c-0305e82c3301"));

        // Becomes the 404 body's detail, so the wording is API surface.
        assertThat(new TapeNotFoundException(id))
            .hasMessage("No tape with id '3f2504e0-4f89-41d3-9a0c-0305e82c3301'")
            .isInstanceOf(RuntimeException.class);
    }
}
