package com.jaqqen.tapeshub.repository;

import com.jaqqen.tapeshub.TapeFixtures;
import com.jaqqen.tapeshub.domain.tape.Tape;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTapeRepositoryTest {

    private final InMemoryTapeRepository repository = new InMemoryTapeRepository();

    @Test
    void savesAndReadsBackATape() {
        Tape tape = TapeFixtures.neonNights();

        repository.save(tape);

        assertThat(repository.findById("neon-nights")).contains(tape);
        assertThat(repository.existsById("neon-nights")).isTrue();
        assertThat(repository.findAll()).containsExactly(tape);
    }

    @Test
    void savingTheSameIdReplacesTheTape() {
        Tape tape = TapeFixtures.neonNights();
        repository.save(tape);

        Tape renamed = new Tape(tape.id(), "NEON DAYS", tape.subtitle(), tape.year(), tape.genre(),
                tape.duration(), tape.rating(), tape.description(), tape.colors(), tape.pattern());
        repository.save(renamed);

        assertThat(repository.findAll()).containsExactly(renamed);
    }

    @Test
    void findAllIsOrderedById() {
        Tape neon = TapeFixtures.neonNights();
        Tape chrome = new Tape("chrome-horizon", "CHROME HORIZON", null, "1984", "Sci-Fi",
                "2h 12min", "PG-13", "Space pioneers.", neon.colors(), neon.pattern());
        repository.save(neon);
        repository.save(chrome);

        assertThat(repository.findAll()).containsExactly(chrome, neon);
    }

    @Test
    void deleteReportsWhetherAnythingWasRemoved() {
        repository.save(TapeFixtures.neonNights());

        assertThat(repository.deleteById("neon-nights")).isTrue();
        assertThat(repository.deleteById("neon-nights")).isFalse();
        assertThat(repository.findById("neon-nights")).isEmpty();
    }

    @Test
    void unknownIdsAreEmpty() {
        assertThat(repository.findById("nope")).isEmpty();
        assertThat(repository.existsById("nope")).isFalse();
        assertThat(repository.findAll()).isEmpty();
    }
}
