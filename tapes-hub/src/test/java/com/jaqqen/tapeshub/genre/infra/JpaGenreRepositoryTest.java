package com.jaqqen.tapeshub.genre.infra;

import com.jaqqen.tapeshub.TestcontainersConfiguration;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.domain.Genre;
import com.jaqqen.tapeshub.genre.domain.GenreInUseException;
import com.jaqqen.tapeshub.genre.domain.GenreName;
import com.jaqqen.tapeshub.genre.domain.GenreRepository;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * The genre module's infrastructure ring against a real Postgres, on the schema Flyway builds - so
 * the mapping, the {@code name} index and the foreign key from {@code tape} are all the production
 * ones. Every test runs in a transaction that is rolled back afterwards.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({TestcontainersConfiguration.class, JpaGenreRepository.class})
class JpaGenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

    @Autowired
    private TestEntityManager em;

    private Genre save(String name, @Nullable String description) {
        return repository.save(Genre.create(new GenreName(name), description));
    }

    @Test
    void savedGenreComesBackWithEveryField() {
        Genre saved = save("Sci-Fi", "Speculative technology and futures.");
        em.flush();
        em.clear();

        assertThat(repository.findById(saved.getId())).hasValueSatisfying(found -> {
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getName()).isEqualTo(new GenreName("Sci-Fi"));
            assertThat(found.getDescription()).isEqualTo("Speculative technology and futures.");
        });
    }

    @Test
    void descriptionIsOptional() {
        Genre saved = repository.save(Genre.create(new GenreName("Western"), null));
        em.flush();
        em.clear();

        assertThat(repository.findById(saved.getId()))
            .hasValueSatisfying(found -> assertThat(found.getDescription()).isNull());
    }

    @Test
    void saveOfAnExistingIdUpdatesRatherThanInserts() {
        Genre saved = save("Sci-Fi", "old");
        em.flush();
        em.clear();

        repository.save(Genre.existing(saved.getId(), new GenreName("Science Fiction"), "new"));
        em.flush();
        em.clear();

        assertThat(repository.findAll()).singleElement()
            .satisfies(found -> assertThat(found.getName()).isEqualTo(new GenreName("Science Fiction")));
    }

    @Test
    void findByIdIsEmptyForAnUnknownId() {
        assertThat(repository.findById(GenreId.newId())).isEmpty();
    }

    @Test
    void findByNameMatchesExactly() {
        save("Horror", null);
        em.flush();
        em.clear();

        assertThat(repository.findByName("Horror")).isPresent();
        assertThat(repository.findByName("horror")).isEmpty();
        assertThat(repository.findByName("Horr")).isEmpty();
    }

    @Test
    void findAllIsSortedByName() {
        save("Western", null);
        save("Action", null);
        save("Sci-Fi", null);
        em.flush();
        em.clear();

        // The controller returns this list as-is, so the ordering is the API's ordering.
        assertThat(repository.findAll()).map(genre -> genre.getName().value())
            .containsExactly("Action", "Sci-Fi", "Western");
    }

    @Test
    void findAllIsEmptyOnAnEmptyTable() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void findAllByIdsSkipsIdsThatDoNotExist() {
        Genre horror = save("Horror", null);
        save("Action", null);
        em.flush();
        em.clear();

        List<Genre> found = repository.findAllByIds(List.of(horror.getId(), GenreId.newId()));

        assertThat(found).map(Genre::getId).containsExactly(horror.getId());
    }

    @Test
    void findAllByIdsOfNothingQueriesNothing() {
        assertThat(repository.findAllByIds(List.of())).isEmpty();
    }

    @Test
    void deleteRemovesTheGenreAndReportsIt() {
        Genre horror = save("Horror", null);
        em.flush();

        assertThat(repository.deleteById(horror.getId())).isTrue();
        em.clear();
        assertThat(repository.findById(horror.getId())).isEmpty();
    }

    @Test
    void deleteOfAnUnknownGenreReportsFalseRatherThanThrowing() {
        // The service turns this into a 404; an EmptyResultDataAccessException would be a 500.
        assertThat(repository.deleteById(GenreId.newId())).isFalse();
    }

    @Test
    void deleteOfAGenreATapeStillPointsAtIsRejected() {
        Genre horror = save("Horror", null);
        em.flush();
        insertTapeReferencing(horror.getId());

        // The adapter flushes the delete itself so the foreign key fails here, where the genre id is
        // still known - not at commit time, far away from anything that could name the cause.
        assertThatExceptionOfType(GenreInUseException.class)
            .isThrownBy(() -> repository.deleteById(horror.getId()))
            .withMessage("Genre '%s' is still in use and cannot be deleted".formatted(horror.getId()));
    }

    /**
     * Written straight to the table rather than through the tape module: what is under test is the
     * {@code fk_tape_genre} constraint, and this module may not reach into the other's internals.
     */
    private void insertTapeReferencing(GenreId genreId) {
        em.getEntityManager().createNativeQuery("""
                INSERT INTO tape (id, title, release_date, genre_id, duration,
                                  central, secondary, accent, label, pattern)
                VALUES (?1, ?2, ?3, ?4, ?5, '#ff006e', '#8338ec', '#ffbe0b', '#1a1a2e', 'stripes')
                """)
            .setParameter(1, UUID.randomUUID())
            .setParameter(2, "NEON NIGHTS")
            .setParameter(3, LocalDate.of(1987, 1, 1))
            .setParameter(4, genreId.value())
            .setParameter(5, 6_840_000)
            .executeUpdate();
        em.flush();
    }
}
