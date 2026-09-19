package com.jaqqen.tapeshub.tape.infra;

import com.jaqqen.tapeshub.TestcontainersConfiguration;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.GenreUsage;
import com.jaqqen.tapeshub.tape.domain.Colors;
import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapeDuration;
import com.jaqqen.tapeshub.tape.domain.TapePattern;
import com.jaqqen.tapeshub.tape.domain.TapeRepository;
import com.jaqqen.tapeshub.tape.domain.TapeTitle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tape module's half of the genre module's {@link GenreUsage} question. Run against a real
 * database because the whole point of it is the query: it replaced {@code fk_tape_genre}'s veto,
 * which stopped firing the moment deletes became soft.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({TestcontainersConfiguration.class, JpaTapeRepository.class, TapeGenreUsage.class})
class TapeGenreUsageTest {

    private static final LocalDate RELEASED = LocalDate.of(1987, 1, 1);
    private static final Colors COLORS = new Colors("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e");

    @Autowired
    private GenreUsage usage;

    @Autowired
    private TapeRepository tapes;

    @Autowired
    private TestEntityManager em;

    private GenreId actionGenreId;

    @BeforeEach
    void insertTheGenreATapeNeeds() {
        actionGenreId = GenreId.newId();
        em.getEntityManager()
            .createNativeQuery("""
                INSERT INTO genre (id, name, description, created_at, modified_at)
                VALUES (?1, ?2, NULL, ?3, ?3)
                """)
            .setParameter(1, actionGenreId.value())
            .setParameter(2, "Action")
            .setParameter(3, Instant.now())
            .executeUpdate();
        em.flush();
    }

    private Tape save() {
        return tapes.save(Tape.create(new TapeTitle("NEON NIGHTS"), null, RELEASED, actionGenreId,
            new TapeDuration(6_840_000), COLORS, TapePattern.STRIPES));
    }

    @Test
    void aGenreNoTapeReferencesIsNotInUse() {
        assertThat(usage.isInUse(actionGenreId)).isFalse();
    }

    @Test
    void aGenreATapeReferencesIsInUse() {
        save();
        em.flush();

        assertThat(usage.isInUse(actionGenreId)).isTrue();
    }

    @Test
    void aGenreOnlyDeletedTapesReferenceIsNotInUse() {
        Tape neon = save();
        em.flush();

        tapes.deleteById(neon.getId());
        em.flush();

        // Deleted tapes are marked as deleted but not gone - therefore: true
        assertThat(usage.isInUse(actionGenreId)).isTrue();
    }

    @Test
    void aGenreNothingHasEverHeardOfIsNotInUse() {
        assertThat(usage.isInUse(GenreId.newId())).isFalse();
    }
}
