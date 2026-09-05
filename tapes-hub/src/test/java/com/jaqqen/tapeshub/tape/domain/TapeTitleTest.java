package com.jaqqen.tapeshub.tape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

/** The same type backs the title and the optional subtitle, so both sets of rules are here. */
class TapeTitleTest {

    private static final int MAX_LENGTH = 255;

    @Test
    void carriesItsValue() {
        assertThat(new TapeTitle("NEON NIGHTS").value()).isEqualTo("NEON NIGHTS");
    }

    @Test
    void rendersAsItsValue() {
        assertThat(new TapeTitle("NEON NIGHTS")).hasToString("NEON NIGHTS");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsBlankTitles(String blank) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new TapeTitle(blank))
            .withMessage("title must not be blank");
    }

    @Test
    void acceptsExactlyTheColumnWidth() {
        assertThatNoException().isThrownBy(() -> new TapeTitle("x".repeat(MAX_LENGTH)));
    }

    @Test
    void rejectsTitlesLongerThanTheColumn() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new TapeTitle("x".repeat(MAX_LENGTH + 1)))
            .withMessage("title must be at most 255 characters");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void ofNullableTurnsAnAbsentSubtitleIntoNull(String absent) {
        // A subtitle is optional, and the wire cannot tell "" from "not sent" - both mean no
        // subtitle, so neither may become a TapeTitle that then fails its own invariant.
        assertThat(TapeTitle.ofNullable(absent)).isNull();
    }

    @Test
    void ofNullableWrapsARealSubtitle() {
        assertThat(TapeTitle.ofNullable("The City Never Sleeps"))
            .isEqualTo(new TapeTitle("The City Never Sleeps"));
    }

    @Test
    void ofNullableStillEnforcesTheLengthLimit() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> TapeTitle.ofNullable("x".repeat(MAX_LENGTH + 1)));
    }

    @Test
    void comparesByValue() {
        assertThat(new TapeTitle("NEON NIGHTS")).isEqualTo(new TapeTitle("NEON NIGHTS"));
    }
}
