package com.jaqqen.tapeshub.service;

import com.jaqqen.tapeshub.TapeFixtures;
import com.jaqqen.tapeshub.controller.dto.tape.CreateTapeRequest;
import com.jaqqen.tapeshub.controller.dto.tape.PatchTapeRequest;
import com.jaqqen.tapeshub.controller.dto.tape.TapeColorsDto;
import com.jaqqen.tapeshub.controller.dto.tape.UpdateTapeRequest;
import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.domain.tape.TapePattern;
import com.jaqqen.tapeshub.exception.TapeNotFoundException;
import com.jaqqen.tapeshub.repository.tape.TapeRepository;
import com.jaqqen.tapeshub.repository.tape.model.TapeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TapeServiceTest {
    private static final TapeColorsDto COLORS =
        new TapeColorsDto("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e");
    private static final UUID UNKNOWN = UUID.fromString("99999999-9999-4999-8999-999999999999");
    @Mock
    private TapeRepository tapeRepository;

    @InjectMocks
    private TapeService service;

    private Tape testTape;

    @BeforeEach
    public void setup() {
        testTape = TapeFixtures.neonNights();
    }

    @Test
    void mintsAnIdOnCreate() {
        when(tapeRepository.save(Mockito.any())).thenReturn(testTape);
        Tape created = service.create(createRequest("NEON NIGHTS"));

        assertThat(created.id()).isNotNull();
        assertThat(created.title()).isEqualTo("NEON NIGHTS");
    }

    @Test
    void findByIdFailsForAnUnknownTape() {
        assertThatThrownBy(() -> service.findById(UNKNOWN))
                .isInstanceOf(TapeNotFoundException.class);
    }

    @Test
    void replaceOverwritesEveryFieldButTheId() {
        Tape created = service.create(createRequest("NEON NIGHTS"));

        when(tapeRepository.existsById(created.id())).thenReturn(true);
        Tape replaced = service.replace(created.id(), new UpdateTapeRequest(
                "NEON DAYS", null, "1991", "Drama", "1h 02min", "PG",
                "Rewritten.", COLORS, TapePattern.WAVES));

        assertThat(replaced.id()).isEqualTo(created.id());
        assertThat(replaced.title()).isEqualTo("NEON DAYS");
        assertThat(replaced.subtitle()).isNull();
        assertThat(replaced.pattern()).isEqualTo(TapePattern.WAVES);
    }

    @Test
    void replaceFailsForAnUnknownTape() {
        assertThatThrownBy(() -> service.replace(UNKNOWN, new UpdateTapeRequest(
                "NEON DAYS", null, "1991", "Drama", "1h 02min", "PG",
                "Rewritten.", COLORS, TapePattern.WAVES)))
                .isInstanceOf(TapeNotFoundException.class);
    }

    @Test
    void patchOnlyAppliesTheFieldsThatWereSent() {
        Tape original = service.create(createRequest("NEON NIGHTS"));

        Tape patched = service.patch(original.id(), new PatchTapeRequest(
                null, null, null, null, null, "PG-13", null, null, null));

        assertThat(patched.rating()).isEqualTo("PG-13");
        assertThat(patched.title()).isEqualTo(original.title());
        assertThat(patched.subtitle()).isEqualTo(original.subtitle());
        assertThat(patched.colors()).isEqualTo(original.colors());
        assertThat(patched.pattern()).isEqualTo(original.pattern());
    }

    @Test
    void deleteRemovesTheTapeAndThenFails() {
        Tape created = service.create(createRequest("NEON NIGHTS"));

        service.delete(created.id());

        assertThat(service.findAll()).isEmpty();
        assertThatThrownBy(() -> service.delete(created.id()))
                .isInstanceOf(TapeNotFoundException.class);
    }

    private static CreateTapeRequest createRequest(String title) {
        Tape template = TapeFixtures.neonNights();
        return new CreateTapeRequest(title, template.subtitle(), template.year(),
                template.genre(), template.duration(), template.rating(), template.description(),
                COLORS, template.pattern());
    }
}
