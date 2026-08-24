package com.jaqqen.tapeshub.repository;

import com.jaqqen.tapeshub.TapeFixtures;
import com.jaqqen.tapeshub.TestcontainersConfiguration;
import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.domain.tape.TapeColors;
import com.jaqqen.tapeshub.domain.tape.TapePattern;
import com.jaqqen.tapeshub.repository.tape.TapeRepository;
import com.jaqqen.tapeshub.repository.tape.model.TapeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the {@link TapeRepository} contract against a real Postgres, so the
 * schema, the Flyway migrations and the entity mapping are all verified together -
 * things an in-memory fake cannot tell us.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class TapeRepositoryTest {

    private static final UUID UNKNOWN = UUID.fromString("99999999-9999-4999-8999-999999999999");

    @Autowired
    private TapeRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void savesAndReadsBackATape() {
        Tape tape = TapeFixtures.neonNights();

        TapeEntity entity = TapeEntity.fromDomain(tape);
        repository.save(entity);
        flush();

        assertThat(repository.findById(tape.id())).contains(entity);
        assertThat(repository.existsById(tape.id())).isTrue();
    }

    @Test
    void roundTripsANullSubtitle() {
        Tape noSubtitle = tape(UUID.randomUUID(), "STEEL RAIN", null);

        TapeEntity entity = TapeEntity.fromDomain(noSubtitle);
        repository.save(entity);
        flush();

        assertThat(repository.findById(noSubtitle.id())).contains(entity);
    }

    /** The reason the upsert keys off the id: an edit must move the row, not add one. */
    @Test
    void aRetitleUpdatesInPlaceAndKeepsTheId() {
        Tape original = TapeFixtures.neonNights();
        TapeEntity entity = TapeEntity.fromDomain(original);

        repository.save(entity);
        flush();

        Tape retitled = tape(original.id(), "NEON DAYS", "A New Subtitle");
        TapeEntity retitledEntity = TapeEntity.fromDomain(retitled);

        repository.save(retitledEntity);
        flush();

        assertThat(repository.findAll()).containsExactly(retitledEntity);
        assertThat(countRows()).isOne();
    }

    @Test
    void allowsTwoTapesToShareATitle() {
        Tape tape1 = tape(UUID.randomUUID(), "NEON NIGHTS", null);
        TapeEntity entity1 = TapeEntity.fromDomain(tape1);

        repository.save(entity1);
        Tape tape2 = tape(UUID.randomUUID(), "NEON NIGHTS", "A Remake");
        TapeEntity entity2 = TapeEntity.fromDomain(tape2);
        repository.save(entity2);
        flush();

        assertThat(countRows()).isEqualTo(2);
    }

    @Test
    void findAllIsOrderedByTitle() {
        Tape tape1 = tape(UUID.randomUUID(), "NEON NIGHTS", null);
        TapeEntity entity1 = TapeEntity.fromDomain(tape1);

        Tape tape2 = tape(UUID.randomUUID(), "VELVET THUNDER", null);
        TapeEntity entity2 = TapeEntity.fromDomain(tape2);

        Tape tape3 = tape(UUID.randomUUID(), "CHROME HORIZON", null);
        TapeEntity entity3 = TapeEntity.fromDomain(tape3);

        repository.save(entity1);
        repository.save(entity2);
        repository.save(entity3);
        flush();

        assertThat(repository.findAll())
                .map(TapeEntity::toDomain)
                .extracting(Tape::title)
                .containsExactly("CHROME HORIZON", "NEON NIGHTS", "VELVET THUNDER");
    }

    @Test
    void persistsThePatternAsItsKebabCaseWireValue() {
        Tape tape = new Tape(UUID.randomUUID(), "TURBO KID", null, "1988", "Adventure",
                "1h 42min", "PG", "Full speed ahead.",
                new TapeColors("#00f5d4", "#00bbf9", "#f15bb5", "#0b132b"),
                TapePattern.RETRO_BLOCKS);
        TapeEntity entity = TapeEntity.fromDomain(tape);

        repository.save(entity);
        flush();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT pattern FROM tapes WHERE id = ?", String.class, tape.id()))
                .isEqualTo("retro-blocks");
        assertThat(repository.findById(tape.id()))
                .map(TapeEntity::toDomain)
                .get()
                .extracting(Tape::pattern)
                .isEqualTo(TapePattern.RETRO_BLOCKS);
    }

    @Test
    void reportsAnUnknownId() {
        assertThat(repository.findById(UNKNOWN)).isEmpty();
        assertThat(repository.existsById(UNKNOWN)).isFalse();
    }

    @Test
    void deleteReturnsTrueOnlyWhenARowWasRemoved() {
        Tape tape = TapeFixtures.neonNights();
        TapeEntity entity = TapeEntity.fromDomain(tape);

        repository.save(entity);
        flush();

        assertThat(repository.removeById(tape.id())).isTrue();
        flush();
        assertThat(repository.removeById(tape.id())).isFalse();
        assertThat(countRows()).isZero();
    }

    /** Pushes pending writes to the database and empties the persistence context, so
     *  the next read comes back from Postgres rather than from Hibernate's cache. */
    private void flush() {
        entityManager.flush();
        entityManager.clear();
    }

    private Integer countRows() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM tapes", Integer.class);
    }

    private static Tape tape(UUID id, String title, String subtitle) {
        Tape base = TapeFixtures.neonNights();
        return new Tape(id, title, subtitle, base.year(), base.genre(), base.duration(),
                base.rating(), base.description(), base.colors(), base.pattern());
    }
}
