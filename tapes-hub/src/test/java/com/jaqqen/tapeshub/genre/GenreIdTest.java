package com.jaqqen.tapeshub.genre;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GenreIdTest {

    @Test
    void newIdIsUnique() {
        assertThat(Stream.generate(GenreId::newId).limit(100).distinct().count()).isEqualTo(100);
    }

    @Test
    void rendersAsThePlainUuid() {
        UUID uuid = UUID.fromString("8e17b20c-0e19-4c68-9eba-f5d5e9e9688d");

        // Exception messages and log lines interpolate the id, so the record's default
        // "GenreId[value=...]" would leak into responses.
        assertThat(new GenreId(uuid)).hasToString("8e17b20c-0e19-4c68-9eba-f5d5e9e9688d");
    }

    @Test
    void comparesByValue() {
        UUID uuid = UUID.randomUUID();

        assertThat(new GenreId(uuid)).isEqualTo(new GenreId(uuid));
    }
}
