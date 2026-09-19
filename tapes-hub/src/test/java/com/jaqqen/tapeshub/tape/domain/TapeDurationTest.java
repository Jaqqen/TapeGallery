package com.jaqqen.tapeshub.tape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class TapeDurationTest {

    @Test
    void carriesMilliseconds() {
        assertThat(new TapeDuration(6_840_000).milliseconds()).isEqualTo(6_840_000);
    }

    @Test
    void acceptsTheSmallestPositiveDuration() {
        assertThat(new TapeDuration(1).milliseconds()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void rejectsDurationsThatAreNotPositive(int invalid) {
        // A tape that runs for no time is not a tape; the DTO's @Positive says the same thing at the edge.
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new TapeDuration(invalid))
            .withMessage("duration must be greater than zero");
    }

    @Test
    void comparesByValue() {
        assertThat(new TapeDuration(1000)).isEqualTo(new TapeDuration(1000));
    }
}
