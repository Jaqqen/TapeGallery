package com.jaqqen.tapeshub.service;

import com.jaqqen.tapeshub.TapeFixtures;
import com.jaqqen.tapeshub.controller.dto.tape.CreateTapeRequest;
import com.jaqqen.tapeshub.controller.dto.tape.PatchTapeRequest;
import com.jaqqen.tapeshub.controller.dto.tape.TapeColorsDto;
import com.jaqqen.tapeshub.controller.dto.tape.UpdateTapeRequest;
import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.domain.tape.TapePattern;
import com.jaqqen.tapeshub.exception.TapeAlreadyExistsException;
import com.jaqqen.tapeshub.exception.TapeNotFoundException;
import com.jaqqen.tapeshub.repository.InMemoryTapeRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TapeServiceTest {

    private static final TapeColorsDto COLORS =
            new TapeColorsDto("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e");

    private final TapeService service = new TapeService(new InMemoryTapeRepository());

    @Test
    void derivesTheIdFromTheTitleWhenNoneIsGiven() {
        Tape created = service.create(createRequest(null, "NEON NIGHTS"));

        assertThat(created.id()).isEqualTo("neon-nights");
    }

    @Test
    void keepsAnExplicitId() {
        Tape created = service.create(createRequest("custom-id", "NEON NIGHTS"));

        assertThat(created.id()).isEqualTo("custom-id");
    }

    @Test
    void slugifyStripsPunctuationAndEdgeDashes() {
        assertThat(TapeService.slugify("  Velvet & Thunder!! ")).isEqualTo("velvet-thunder");
        assertThat(TapeService.slugify("Chrome Horizon 2")).isEqualTo("chrome-horizon-2");
    }

    @Test
    void rejectsATitleThatCannotBecomeAnId() {
        assertThatThrownBy(() -> service.create(createRequest(null, "!!!")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsADuplicateId() {
        service.create(createRequest(null, "NEON NIGHTS"));

        assertThatThrownBy(() -> service.create(createRequest(null, "Neon Nights")))
                .isInstanceOf(TapeAlreadyExistsException.class);
    }

    @Test
    void findByIdFailsForAnUnknownTape() {
        assertThatThrownBy(() -> service.findById("nope"))
                .isInstanceOf(TapeNotFoundException.class);
    }

    @Test
    void replaceOverwritesEveryField() {
        service.create(createRequest(null, "NEON NIGHTS"));

        Tape replaced = service.replace("neon-nights", new UpdateTapeRequest(
                null, "NEON DAYS", null, "1991", "Drama", "1h 02min", "PG",
                "Rewritten.", COLORS, TapePattern.WAVES));

        assertThat(replaced.title()).isEqualTo("NEON DAYS");
        assertThat(replaced.subtitle()).isNull();
        assertThat(replaced.pattern()).isEqualTo(TapePattern.WAVES);
        assertThat(replaced.id()).isEqualTo("neon-nights");
    }

    @Test
    void replaceRejectsAMismatchedBodyId() {
        service.create(createRequest(null, "NEON NIGHTS"));

        assertThatThrownBy(() -> service.replace("neon-nights", new UpdateTapeRequest(
                "other-id", "NEON DAYS", null, "1991", "Drama", "1h 02min", "PG",
                "Rewritten.", COLORS, TapePattern.WAVES)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replaceFailsForAnUnknownTape() {
        assertThatThrownBy(() -> service.replace("nope", new UpdateTapeRequest(
                null, "NEON DAYS", null, "1991", "Drama", "1h 02min", "PG",
                "Rewritten.", COLORS, TapePattern.WAVES)))
                .isInstanceOf(TapeNotFoundException.class);
    }

    @Test
    void patchOnlyAppliesTheFieldsThatWereSent() {
        Tape original = service.create(createRequest(null, "NEON NIGHTS"));

        Tape patched = service.patch("neon-nights", new PatchTapeRequest(
                null, null, null, null, null, "PG-13", null, null, null));

        assertThat(patched.rating()).isEqualTo("PG-13");
        assertThat(patched.title()).isEqualTo(original.title());
        assertThat(patched.subtitle()).isEqualTo(original.subtitle());
        assertThat(patched.colors()).isEqualTo(original.colors());
        assertThat(patched.pattern()).isEqualTo(original.pattern());
    }

    @Test
    void deleteRemovesTheTapeAndThenFails() {
        service.create(createRequest(null, "NEON NIGHTS"));

        service.delete("neon-nights");

        assertThat(service.findAll()).isEmpty();
        assertThatThrownBy(() -> service.delete("neon-nights"))
                .isInstanceOf(TapeNotFoundException.class);
    }

    private static CreateTapeRequest createRequest(String id, String title) {
        Tape template = TapeFixtures.neonNights();
        return new CreateTapeRequest(id, title, template.subtitle(), template.year(),
                template.genre(), template.duration(), template.rating(), template.description(),
                COLORS, template.pattern());
    }
}
