package com.jaqqen.tapeshub.tape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ColorsTest {

    @SuppressWarnings("UnusedReturnValue")
    private static Colors with(String primary) {
        return new Colors(primary, "#8338ec", "#ffbe0b", "#1a1a2e");
    }

    @Test
    void keepsTheFourSleeveColours() {
        Colors colors = new Colors("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e");

        assertThat(colors.primary()).isEqualTo("#ff006e");
        assertThat(colors.secondary()).isEqualTo("#8338ec");
        assertThat(colors.accent()).isEqualTo("#ffbe0b");
        assertThat(colors.label()).isEqualTo("#1a1a2e");
    }

    @ParameterizedTest
    @ValueSource(strings = {"#fff", "#FFF", "#ff006e", "#FF006E", "#Ff006E"})
    void acceptsThreeAndSixDigitHexInEitherCase(String hex) {
        assertThatNoException().isThrownBy(() -> with(hex));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ff006e", "#ff006", "#ff006ee", "#ff", "#ggghhh", "#ff006e ", "red", "", "#"})
    void rejectsAnythingThatIsNotAHexColour(String notHex) {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> with(notHex))
            .withMessageContaining("primary must be a hex colour");
    }

    @Test
    void namesWhichOfTheFourWasWrong() {
        // Four identical-looking strings; without the name the message would be useless.
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Colors("#ff006e", "#8338ec", "nope", "#1a1a2e"))
            .withMessage("accent must be a hex colour such as #ff006e, but was 'nope'");
    }

    @Test
    void theHexPatternIsTheOneTheRequestDtoValidatesWith() {
        // TapeColorsDto reuses Colors.HEX_PATTERN so a bad payload is a 400 rather than reaching
        // the domain and surfacing as a 500 - the two must not be allowed to drift apart.
        assertThat("#ff006e".matches(Colors.HEX_PATTERN)).isTrue();
        assertThat("nope".matches(Colors.HEX_PATTERN)).isFalse();
    }

    @Test
    void comparesByValue() {
        assertThat(new Colors("#fff", "#000", "#f00", "#00f"))
            .isEqualTo(new Colors("#fff", "#000", "#f00", "#00f"));
    }
}
