package com.jaqqen.tapeshub.tape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * The wire values are shared with {@code web-portal}, so they are a contract rather than an
 * implementation detail - renaming a constant is free, changing its value is a breaking change.
 */
class TapePatternTest {

    @Test
    void wireValuesAreTheOnesTheFrontendUses() {
        assertThat(TapePattern.values()).extracting(TapePattern::getValue)
            .containsExactly("stripes", "gradient", "geometric", "retro-blocks", "waves", "diamonds");
    }

    @Test
    void multiWordPatternsAreKebabCaseNotTheConstantName() {
        assertThat(TapePattern.RETRO_BLOCKS.getValue()).isEqualTo("retro-blocks");
    }

    @ParameterizedTest
    @EnumSource(TapePattern.class)
    void everyPatternRoundTripsThroughItsWireValue(TapePattern pattern) {
        assertThat(TapePattern.fromValue(pattern.getValue())).isSameAs(pattern);
    }

    @Test
    void rejectsAnUnknownWireValue() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> TapePattern.fromValue("plaid"))
            .withMessage("Unknown tape pattern: plaid");
    }

    @Test
    void rejectsTheConstantNameItself() {
        // Jackson deserialises through fromValue, so accepting "RETRO_BLOCKS" would quietly let a
        // second spelling of every pattern into the API.
        assertThatIllegalArgumentException().isThrownBy(() -> TapePattern.fromValue("RETRO_BLOCKS"));
    }
}
