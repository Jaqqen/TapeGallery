package com.jaqqen.tapeshub.tape.infra;

import com.jaqqen.tapeshub.TestcontainersConfiguration;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.tape.domain.Colors;
import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapeDuration;
import com.jaqqen.tapeshub.tape.domain.TapeId;
import com.jaqqen.tapeshub.tape.domain.TapePattern;
import com.jaqqen.tapeshub.tape.domain.TapeRepository;
import com.jaqqen.tapeshub.tape.domain.TapeTitle;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * The tape module's infrastructure ring against a real Postgres. Worth doing over a real database
 * rather than a mock: the column the first colour lands in is not called {@code primary}, and the
 * {@code genre_id} foreign key is a rule that only exists in storage.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({TestcontainersConfiguration.class, JpaTapeRepository.class})
class JpaTapeRepositoryTest {

    private static final LocalDate RELEASED = LocalDate.of(1987, 1, 1);
    private static final Colors COLORS = new Colors("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e");

    @Autowired
    private TapeRepository repository;

    @Autowired
    private TestEntityManager em;

    private GenreId action;

    @BeforeEach
    void insertTheGenreATapeNeeds() {
        // A tape cannot exist without a genre, and this module may not reach into the genre
        // module's internals to make one - so the row goes in directly.
        action = GenreId.newId();
        em.getEntityManager()
            .createNativeQuery("INSERT INTO genre (id, name, description) VALUES (?1, ?2, NULL)")
            .setParameter(1, action.value())
            .setParameter(2, "Action")
            .executeUpdate();
        em.flush();
    }

    private Tape save(String title, @Nullable String subtitle) {
        return repository.save(Tape.create(new TapeTitle(title), TapeTitle.ofNullable(subtitle),
            RELEASED, action, new TapeDuration(6_840_000), COLORS, TapePattern.STRIPES));
    }

    @Test
    void savedTapeComesBackWithEveryField() {
        Tape saved = save("NEON NIGHTS", "The City Never Sleeps");
        em.flush();
        em.clear();

        assertThat(repository.findById(saved.getId())).hasValueSatisfying(found -> {
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getTitle()).isEqualTo(new TapeTitle("NEON NIGHTS"));
            assertThat(found.getSubtitle()).isEqualTo(new TapeTitle("The City Never Sleeps"));
            assertThat(found.getReleaseDate()).isEqualTo(RELEASED);
            assertThat(found.getGenre()).isEqualTo(action);
            assertThat(found.getDuration()).isEqualTo(new TapeDuration(6_840_000));
            assertThat(found.getColors()).isEqualTo(COLORS);
            assertThat(found.getPattern()).isEqualTo(TapePattern.STRIPES);
        });
    }

    @Test
    void subtitleIsOptional() {
        Tape saved = save("VELVET THUNDER", null);
        em.flush();
        em.clear();

        assertThat(repository.findById(saved.getId()))
            .hasValueSatisfying(found -> assertThat(found.getSubtitle()).isNull());
    }

    @Test
    void thePrimaryColourIsStoredInTheCentralColumn() {
        Tape saved = save("NEON NIGHTS", null);
        em.flush();

        // 'primary' is a reserved SQL keyword, so the column is 'central'. The rename lives only in
        // TapeMapper and TapeColorsEmbeddable - nothing else would notice if it silently broke.
        Object central = em.getEntityManager()
            .createNativeQuery("SELECT central FROM tape WHERE id = ?1")
            .setParameter(1, saved.getId().value())
            .getSingleResult();

        assertThat(central).isEqualTo("#ff006e");
    }

    @Test
    void thePatternIsStoredAsItsWireValueNotItsConstantName() {
        Tape saved = repository.save(Tape.create(new TapeTitle("SOLAR BURN"), null, RELEASED, action,
            new TapeDuration(1000), COLORS, TapePattern.RETRO_BLOCKS));
        em.flush();

        Object pattern = em.getEntityManager()
            .createNativeQuery("SELECT pattern FROM tape WHERE id = ?1")
            .setParameter(1, saved.getId().value())
            .getSingleResult();

        assertThat(pattern).isEqualTo("retro-blocks");
    }

    @Test
    void saveOfAnExistingIdUpdatesRatherThanInserts() {
        Tape saved = save("NEON NIGHTS", null);
        em.flush();
        em.clear();

        repository.save(Tape.existing(saved.getId(), new TapeTitle("NEON NIGHTS II"), null, RELEASED,
            action, new TapeDuration(1000), COLORS, TapePattern.WAVES));
        em.flush();
        em.clear();

        assertThat(repository.findAll()).singleElement()
            .satisfies(found -> assertThat(found.getTitle()).isEqualTo(new TapeTitle("NEON NIGHTS II")));
    }

    @Test
    void findByIdIsEmptyForAnUnknownId() {
        assertThat(repository.findById(TapeId.newId())).isEmpty();
    }

    @Test
    void findAllIsSortedByTitle() {
        save("VELVET THUNDER", null);
        save("CHROME HORIZON", null);
        save("NEON NIGHTS", null);
        em.flush();
        em.clear();

        assertThat(repository.findAll()).map(tape -> tape.getTitle().value())
            .containsExactly("CHROME HORIZON", "NEON NIGHTS", "VELVET THUNDER");
    }

    @Test
    void findAllIsEmptyOnAnEmptyTable() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void deleteRemovesTheTapeAndReportsIt() {
        Tape neon = save("NEON NIGHTS", null);
        em.flush();

        assertThat(repository.deleteById(neon.getId())).isTrue();
        // Unlike the genre adapter, this one does not flush the delete itself - it has no foreign
        // key to report on - so the flush that reaches the database is the test's to do.
        em.flush();
        em.clear();
        assertThat(repository.findById(neon.getId())).isEmpty();
    }

    @Test
    void deleteOfAnUnknownTapeReportsFalseRatherThanThrowing() {
        assertThat(repository.deleteById(TapeId.newId())).isFalse();
    }

    @Test
    void aTapePointingAtAGenreThatDoesNotExistCannotBeStored() {
        repository.save(Tape.create(new TapeTitle("GHOST"), null, RELEASED, GenreId.newId(),
            new TapeDuration(1000), COLORS, TapePattern.STRIPES));

        // fk_tape_genre is what makes "a tape must have a genre" true in storage, not just in the
        // service that checks it first. It surfaces raw here: nothing in this module flushes on the
        // caller's behalf, so there is no Spring translation on the way out either.
        assertThatExceptionOfType(ConstraintViolationException.class)
            .isThrownBy(em::flush)
            .withMessageContaining("fk_tape_genre");
    }

    @Test
    void everyTapeIdIsDistinctFromItsGenreId() {
        Tape neon = save("NEON NIGHTS", null);

        assertThat(neon.getId().value()).isNotEqualTo(action.value());
        assertThat(neon.getId().value()).isInstanceOf(UUID.class);
    }
}
