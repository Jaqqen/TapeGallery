package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Both exceptions become a response body, so their messages are API surface, not just log text.
 */
class GenreExceptionsTest {

    private static final GenreId ID = new GenreId(UUID.fromString("8e17b20c-0e19-4c68-9eba-f5d5e9e9688d"));

    @Test
    void notFoundNamesTheGenreItLookedFor() {
        assertThat(new GenreNotFoundException(ID))
            .hasMessage("No genre with id '8e17b20c-0e19-4c68-9eba-f5d5e9e9688d'")
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void inUseSaysWhyTheDeleteWasRefused() {
        assertThat(new GenreInUseException(ID))
            .hasMessage("Genre '8e17b20c-0e19-4c68-9eba-f5d5e9e9688d' is still in use and cannot be deleted")
            .isInstanceOf(RuntimeException.class);
    }
}
