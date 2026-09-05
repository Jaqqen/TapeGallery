package com.jaqqen.tapeshub.tape.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TapeTest {

    private static final TapeTitle TITLE = new TapeTitle("NEON NIGHTS");
    private static final TapeTitle SUBTITLE = new TapeTitle("The City Never Sleeps");
    private static final LocalDate RELEASED = LocalDate.of(1987, 1, 1);
    private static final TapeDuration DURATION = new TapeDuration(6_840_000);
    private static final Colors COLORS = new Colors("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e");

    private static Tape tape() {
        return Tape.create(TITLE, SUBTITLE, RELEASED, GenreId.newId(), DURATION, COLORS,
            TapePattern.STRIPES);
    }

    @Test
    void createMintsItsOwnIdentity() {
        Tape first = tape();
        Tape second = tape();

        assertThat(first.getId()).isNotNull();
        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    void createKeepsWhatItWasGiven() {
        GenreId genre = GenreId.newId();

        Tape tape = Tape.create(TITLE, SUBTITLE, RELEASED, genre, DURATION, COLORS, TapePattern.WAVES);

        assertThat(tape.getTitle()).isEqualTo(TITLE);
        assertThat(tape.getSubtitle()).isEqualTo(SUBTITLE);
        assertThat(tape.getReleaseDate()).isEqualTo(RELEASED);
        assertThat(tape.getGenre()).isEqualTo(genre);
        assertThat(tape.getDuration()).isEqualTo(DURATION);
        assertThat(tape.getColors()).isEqualTo(COLORS);
        assertThat(tape.getPattern()).isEqualTo(TapePattern.WAVES);
    }

    @Test
    void createAllowsNoSubtitle() {
        Tape tape = Tape.create(TITLE, null, RELEASED, GenreId.newId(), DURATION, COLORS,
            TapePattern.STRIPES);

        assertThat(tape.getSubtitle()).isNull();
    }

    @Test
    void existingCarriesThePersistedIdentity() {
        TapeId id = TapeId.newId();

        Tape tape = Tape.existing(id, TITLE, null, RELEASED, GenreId.newId(), DURATION, COLORS,
            TapePattern.STRIPES);

        assertThat(tape.getId()).isEqualTo(id);
    }

    @Test
    void aTapeRefersToItsGenreByIdentityAlone() {
        // The aggregates are separate: a Tape never holds a Genre object, only its id. jMolecules
        // enforces this in ModularityTests; this states the rule in the tape module's own terms.
        assertThat(tape().getGenre()).isInstanceOf(GenreId.class);
    }

    @Test
    void everyOperationLeavesTheIdentityAlone() {
        Tape tape = tape();
        TapeId id = tape.getId();

        tape.rename(new TapeTitle("CHROME HORIZON"))
            .releasedOn(LocalDate.of(1984, 1, 1))
            .reclassify(GenreId.newId())
            .runsFor(new TapeDuration(1))
            .recolour(new Colors("#fff", "#000", "#f00", "#00f"))
            .restyle(TapePattern.DIAMONDS);

        assertThat(tape.getId()).isEqualTo(id);
    }

    @Test
    void operationsMutateAndChain() {
        Tape tape = tape();

        Tape renamed = tape.rename(new TapeTitle("CHROME HORIZON"));

        assertThat(renamed).isSameAs(tape);
        assertThat(tape.getTitle()).isEqualTo(new TapeTitle("CHROME HORIZON"));
    }

    @Test
    void resubtitleReplacesTheSubtitle() {
        Tape tape = tape();

        tape.resubtitle(new TapeTitle("Beyond the Last Frontier"));

        assertThat(tape.getSubtitle()).isEqualTo(new TapeTitle("Beyond the Last Frontier"));
    }

    @Test
    void resubtitleWithNullKeepsTheCurrentSubtitle() {
        Tape tape = tape();

        tape.resubtitle(null);

        // Deliberately unlike Genre.describe(null), which clears. PATCH relies on it: an omitted
        // subtitle must leave the stored one alone. The consequence is that a subtitle can be
        // changed but never removed - see replaceWithLeavesAnOmittedSubtitleInPlace.
        assertThat(tape.getSubtitle()).isEqualTo(SUBTITLE);
    }

    @Test
    void replaceWithOverwritesEveryOtherField() {
        Tape tape = tape();
        GenreId genre = GenreId.newId();
        Colors colors = new Colors("#00b4d8", "#0077b6", "#90e0ef", "#023e8a");

        tape.replaceWith(new TapeTitle("CHROME HORIZON"), new TapeTitle("Beyond the Last Frontier"),
            LocalDate.of(1984, 1, 1), genre, new TapeDuration(7_920_000), colors, TapePattern.GRADIENT);

        assertThat(tape.getTitle()).isEqualTo(new TapeTitle("CHROME HORIZON"));
        assertThat(tape.getSubtitle()).isEqualTo(new TapeTitle("Beyond the Last Frontier"));
        assertThat(tape.getReleaseDate()).isEqualTo(LocalDate.of(1984, 1, 1));
        assertThat(tape.getGenre()).isEqualTo(genre);
        assertThat(tape.getDuration()).isEqualTo(new TapeDuration(7_920_000));
        assertThat(tape.getColors()).isEqualTo(colors);
        assertThat(tape.getPattern()).isEqualTo(TapePattern.GRADIENT);
    }

    @Test
    void replaceWithLeavesAnOmittedSubtitleInPlace() {
        Tape tape = tape();

        tape.replaceWith(TITLE, null, RELEASED, GenreId.newId(), DURATION, COLORS, TapePattern.STRIPES);

        // Current behaviour, pinned rather than endorsed: replaceWith delegates to resubtitle, so a
        // PUT that omits the subtitle does not blank it the way a full replace otherwise would.
        assertThat(tape.getSubtitle()).isEqualTo(SUBTITLE);
    }
}
