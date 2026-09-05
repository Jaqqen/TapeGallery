package com.jaqqen.tapeshub.tape.app;

import com.jaqqen.tapeshub.genre.GenreId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnknownGenreExceptionTest {

    @Test
    void namesTheGenreThatCouldNotBeResolved() {
        GenreId id = new GenreId(UUID.fromString("bcbba66a-c4d1-431a-bf61-47f767996b32"));

        assertThat(new UnknownGenreException(id))
            .hasMessage("No genre with id 'bcbba66a-c4d1-431a-bf61-47f767996b32'")
            .isInstanceOf(RuntimeException.class);
    }
}
