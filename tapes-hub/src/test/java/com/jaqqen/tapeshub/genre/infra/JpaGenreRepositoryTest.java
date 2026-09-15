package com.jaqqen.tapeshub.genre.infra;

import com.jaqqen.tapeshub.TestcontainersConfiguration;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.domain.Genre;
import com.jaqqen.tapeshub.genre.domain.GenreName;
import com.jaqqen.tapeshub.genre.domain.GenreRepository;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Genre saved = save("Western", null);
        em.flush();
        em.clear();

        assertThat(repository.findById(saved.getId()))
            .hasValueSatisfying(found -> assertThat(found.getDescription()).isNull());
    }

    @Test
    void testThatSaveOnExistingEntityUpdatesIt() {
        Genre saved = save("Sci-Fi", "old");
        em.flush();
        em.clear();

        repository.save(Genre.existing(saved.getId(), new GenreName("Science Fiction"), "new",
            saved.getLifecycle()));
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
    void findAllByIdsSkipsIdsThatDoNotExist() {
        Genre horror = save("Horror", null);
        Genre action = save("Action", null);
        em.flush();
        em.clear();

        List<Genre> found = repository.findAllByIds(List.of(horror.getId(), GenreId.newId()));

        final AbstractListAssert<?, List<? extends GenreId>, GenreId, ObjectAssert<GenreId>> map = assertThat(found)
            .map(Genre::getId);
        map.containsExactly(horror.getId());
        map.doesNotContain(action.getId());
    }

    @Test
    void findAllByIdsOfNothingQueriesNothing() {
        assertThat(repository.findAllByIds(List.of())).isEmpty();
    }

    @Test
    void testLifecyclePersistsCorrectly() {
        Genre saved = save("Sci-Fi", null);
        em.flush();
        em.clear();

        // The stamps are truncated to microseconds due to timestamptz
        assertThat(repository.findById(saved.getId()))
            .hasValueSatisfying(found -> assertThat(found.getLifecycle()).isEqualTo(saved.getLifecycle()));
    }

    @Test
    void deleteMarksTheGenreAndReportsIt() {
        Genre horror = save("Horror", null);
        em.flush();

        assertThat(repository.softDeleteById(horror.getId())).isTrue();
        em.flush();
        em.clear();
        assertThat(repository.findById(horror.getId())).isEmpty();
    }

    @Test
    void deleteLeavesTheRowInPlaceWithADeletedAtStamp() {
        Genre horror = save("Horror", null);
        em.flush();
        repository.softDeleteById(horror.getId());
        em.flush();

        Object deletedAt = em.getEntityManager()
            .createNativeQuery("SELECT deleted_at FROM genre WHERE id = ?1")
            .setParameter(1, horror.getId().value())
            .getSingleResult();

        assertThat(deletedAt).isNotNull();
    }

    @Test
    void deletingTwiceReportsFalseTheSecondTime() {
        Genre horror = save("Horror", null);
        em.flush();

        assertThat(repository.softDeleteById(horror.getId())).isTrue();
        em.flush();
        assertThat(repository.softDeleteById(horror.getId())).isFalse();
    }

    @Test
    void deleteOfAnUnknownGenreReportsFalseRatherThanThrowing() {
        assertThat(repository.softDeleteById(GenreId.newId())).isFalse();
    }

    @Test
    void aDeletedGenreIsHiddenFromEveryFind() {
        Genre horror = save("Horror", null);
        Genre action = save("Action", null);
        em.flush();
        repository.softDeleteById(horror.getId());
        em.flush();
        em.clear();

        assertThat(repository.findById(horror.getId())).isEmpty();
        assertThat(repository.findByName("Horror")).isEmpty();
        assertThat(repository.findAll()).map(Genre::getId).containsExactly(action.getId());
        assertThat(repository.findAllByIds(List.of(horror.getId(), action.getId())))
            .map(Genre::getId).containsExactly(action.getId());
    }
}
