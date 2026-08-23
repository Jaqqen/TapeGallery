package com.jaqqen.tapeshub.repository;

import com.jaqqen.tapeshub.TapeFixtures;
import com.jaqqen.tapeshub.domain.tape.Tape;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTapeRepositoryTest {

    private static final UUID UNKNOWN = UUID.fromString("99999999-9999-4999-8999-999999999999");

    private final InMemoryTapeRepository repository = new InMemoryTapeRepository();

    @Test
    void savesAndReadsBackATape() {
        Tape tape = TapeFixtures.neonNights();

        repository.save(tape);

        assertThat(repository.findById(tape.id())).contains(tape);
        assertThat(repository.existsById(tape.id())).isTrue();
        assertThat(repository.findAll()).containsExactly(tape);
    }

    @Test
    void savingTheSameIdReplacesTheTape() {
        Tape tape = TapeFixtures.neonNights();
        repository.save(tape);

        Tape renamed = new Tape(tape.id(), "NEON DAYS", tape.subtitle(), tape.year(),
                tape.genre(), tape.duration(), tape.rating(), tape.description(), tape.colors(),
                tape.pattern());
        repository.save(renamed);

        assertThat(repository.findAll()).containsExactly(renamed);
    }

    @Test
    void findAllIsOrderedByTitle() {
        Tape neon = TapeFixtures.neonNights();
        Tape chrome = new Tape(UUID.randomUUID(), "CHROME HORIZON", null, "1984",
                "Sci-Fi", "2h 12min", "PG-13", "Space pioneers.", neon.colors(), neon.pattern());
        repository.save(neon);
        repository.save(chrome);

        assertThat(repository.findAll()).containsExactly(chrome, neon);
    }

    @Test
    void deleteReportsWhetherAnythingWasRemoved() {
        Tape tape = TapeFixtures.neonNights();
        repository.save(tape);

        assertThat(repository.deleteById(tape.id())).isTrue();
        assertThat(repository.deleteById(tape.id())).isFalse();
        assertThat(repository.findById(tape.id())).isEmpty();
    }

    @Test
    void unknownIdsAreEmpty() {
        assertThat(repository.findById(UNKNOWN)).isEmpty();
        assertThat(repository.existsById(UNKNOWN)).isFalse();
        assertThat(repository.findAll()).isEmpty();
    }
}
