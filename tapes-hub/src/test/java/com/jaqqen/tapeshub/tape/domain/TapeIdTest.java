package com.jaqqen.tapeshub.tape.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TapeIdTest {

    @Test
    void newIdIsUnique() {
        assertThat(Stream.generate(TapeId::newId).limit(100).distinct().count()).isEqualTo(100);
    }

    @Test
    void rendersAsThePlainUuid() {
        UUID uuid = UUID.fromString("3f2504e0-4f89-41d3-9a0c-0305e82c3301");

        // TapeNotFoundException interpolates the id straight into the response detail.
        assertThat(new TapeId(uuid)).hasToString("3f2504e0-4f89-41d3-9a0c-0305e82c3301");
    }

    @Test
    void comparesByValue() {
        UUID uuid = UUID.randomUUID();

        assertThat(new TapeId(uuid)).isEqualTo(new TapeId(uuid));
    }
}
