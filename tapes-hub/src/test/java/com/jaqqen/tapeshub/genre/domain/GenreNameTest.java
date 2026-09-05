package com.jaqqen.tapeshub.genre.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

class GenreNameTest {

    private static final int MAX_LENGTH = 64;

    @Test
    void carriesItsValue() {
        assertThat(new GenreName("Sci-Fi").value()).isEqualTo("Sci-Fi");
    }

    @Test
    void rendersAsItsValue() {
        // The exception messages interpolate the name directly, so toString is part of the contract.
        assertThat(new GenreName("Sci-Fi")).hasToString("Sci-Fi");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void rejectsBlankNames(String blank) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new GenreName(blank))
            .withMessage("genre name must not be blank");
    }

    @Test
    void acceptsExactlyTheColumnWidth() {
        // 64 is the genre.name column width; one character more would fail on insert instead.
        assertThatNoException().isThrownBy(() -> new GenreName("x".repeat(MAX_LENGTH)));
    }

    @Test
    void rejectsNamesLongerThanTheColumn() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new GenreName("x".repeat(MAX_LENGTH + 1)))
            .withMessage("genre name must be at most 64 characters");
    }

    @Test
    void comparesByValue() {
        assertThat(new GenreName("Horror")).isEqualTo(new GenreName("Horror"));
        assertThat(new GenreName("Horror")).isNotEqualTo(new GenreName("horror"));
    }
}
