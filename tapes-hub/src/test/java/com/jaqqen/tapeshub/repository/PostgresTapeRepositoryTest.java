package com.jaqqen.tapeshub.repository;

import com.jaqqen.tapeshub.TapeFixtures;
import com.jaqqen.tapeshub.TestcontainersConfiguration;
import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.domain.tape.TapeColors;
import com.jaqqen.tapeshub.domain.tape.TapePattern;
import com.jaqqen.tapeshub.repository.tape.PostgresTapeRepository;
import com.jaqqen.tapeshub.repository.tape.TapeRepository;
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
 * schema, the Flyway migration and the entity mapping are all verified together -
 * things an in-memory fake cannot tell us.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, PostgresTapeRepository.class})
class PostgresTapeRepositoryTest {

    @Autowired
    private PostgresTapeRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void savesAndReadsBackATape() {
        Tape tape = TapeFixtures.neonNights();

        repository.save(tape);
        flush();

        assertThat(repository.findById("neon-nights")).contains(tape);
        assertThat(repository.existsById("neon-nights")).isTrue();
    }

    @Test
    void roundTripsANullSubtitle() {
        Tape noSubtitle = withSlug("steel-rain", null);

        repository.save(noSubtitle);
        flush();

        assertThat(repository.findById("steel-rain")).contains(noSubtitle);
    }

    @Test
    void savingTheSameSlugUpdatesInPlaceAndKeepsTheSurrogateId() {
        repository.save(TapeFixtures.neonNights());
        flush();
        UUID originalId = surrogateIdOf("neon-nights");

        Tape renamed = withSlug("neon-nights", "A New Subtitle");
        repository.save(renamed);
        flush();

        assertThat(repository.findAll()).containsExactly(renamed);
        assertThat(countRows()).isOne();
        assertThat(surrogateIdOf("neon-nights")).isEqualTo(originalId);
    }

    @Test
    void findAllIsOrderedBySlug() {
        repository.save(withSlug("velvet-thunder", null));
        repository.save(withSlug("chrome-horizon", null));
        repository.save(withSlug("neon-nights", null));
        flush();

        assertThat(repository.findAll())
                .extracting(Tape::id)
                .containsExactly("chrome-horizon", "neon-nights", "velvet-thunder");
    }

    @Test
    void persistsThePatternAsItsKebabCaseWireValue() {
        Tape tape = new Tape("turbo-kid", "TURBO KID", null, "1988", "Adventure", "1h 42min",
                "PG", "Full speed ahead.",
                new TapeColors("#00f5d4", "#00bbf9", "#f15bb5", "#0b132b"),
                TapePattern.RETRO_BLOCKS);

        repository.save(tape);
        flush();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT pattern FROM tapes WHERE slug = 'turbo-kid'", String.class))
                .isEqualTo("retro-blocks");
        assertThat(repository.findById("turbo-kid"))
                .get()
                .extracting(Tape::pattern)
                .isEqualTo(TapePattern.RETRO_BLOCKS);
    }

    @Test
    void reportsAnUnknownId() {
        assertThat(repository.findById("does-not-exist")).isEmpty();
        assertThat(repository.existsById("does-not-exist")).isFalse();
    }

    @Test
    void deleteReturnsTrueOnlyWhenARowWasRemoved() {
        repository.save(TapeFixtures.neonNights());
        flush();

        assertThat(repository.deleteById("neon-nights")).isTrue();
        flush();
        assertThat(repository.deleteById("neon-nights")).isFalse();
        assertThat(countRows()).isZero();
    }

    /** Pushes pending writes to the database and empties the persistence context, so
     *  the next read comes back from Postgres rather than from Hibernate's cache. */
    private void flush() {
        entityManager.flush();
        entityManager.clear();
    }

    private UUID surrogateIdOf(String slug) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM tapes WHERE slug = ?", UUID.class, slug);
    }

    private Integer countRows() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM tapes", Integer.class);
    }

    private static Tape withSlug(String slug, String subtitle) {
        Tape base = TapeFixtures.neonNights();
        return new Tape(slug, base.title(), subtitle, base.year(), base.genre(), base.duration(),
                base.rating(), base.description(), base.colors(), base.pattern());
    }
}
