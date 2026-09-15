package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.shared.Lifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GenreTest {

    private static final GenreName SCI_FI = new GenreName("Sci-Fi");
    private Genre createdSciFi;
    private GenreId createdGenreId;
    private Genre existingSciFi;
    private static final Instant NOON = Instant.parse("2026-09-10T12:00:00Z");
    private static final Lifecycle STORED = new Lifecycle(NOON, NOON, null);

    @BeforeEach
    void setUp() {
        createdSciFi = Genre.create(SCI_FI, "Speculative technology and futures.");
        createdGenreId = createdSciFi.getId();
        existingSciFi = Genre.existing(GenreId.newId(), SCI_FI, "desc", STORED);
    }

    @Test
    void testCorrectGenreIdCreation() {
        Genre second = Genre.create(SCI_FI, null);

        assertThat(createdGenreId).isNotNull();
        assertThat(createdGenreId).isNotEqualTo(second.getId());
    }

    @Test
    void checkCorrectnessOfGenreProperties() {
        assertThat(createdSciFi.getName()).isEqualTo(SCI_FI);
        assertThat(createdSciFi.getDescription()).isEqualTo("Speculative technology and futures.");
    }

    @Test
    void createAllowsNoDescription() {
        assertThat(Genre.create(SCI_FI, null).getDescription()).isNull();
    }

    @Test
    void existingCarriesThePersistedIdentity() {
        final GenreId id = GenreId.newId();
        final Genre genre = Genre.existing(id, SCI_FI, "desc", STORED);

        assertThat(genre.getId()).isEqualTo(id);
    }

    @Test
    void existingDoesNotModifyTheLifecycle() {
        assertThat(existingSciFi.getLifecycle()).isEqualTo(STORED);
    }

    @Test
    void createStartsWithNewLifecycle() {
        assertThat(createdSciFi.getLifecycle().createdAt()).isEqualTo(createdSciFi.getLifecycle().modifiedAt());
        assertThat(createdSciFi.getLifecycle().deletedAt()).isNull();
        assertThat(createdSciFi.isDeleted()).isFalse();
    }

    @Test
    void testRenameAltersModifiedAt() {
        existingSciFi.rename(new GenreName("Science Fiction"));

        assertThat(existingSciFi.getLifecycle().createdAt()).isEqualTo(NOON);
        assertThat(existingSciFi.getLifecycle().modifiedAt()).isAfter(NOON);
    }

    @Test
    void testDescribe() {
        existingSciFi.describe("new");

        assertThat(existingSciFi.getLifecycle().createdAt()).isEqualTo(NOON);
        assertThat(existingSciFi.getLifecycle().modifiedAt()).isAfter(NOON);
        assertThat(existingSciFi.getDescription()).isEqualTo("new");
    }

    @Test
    void checkConditionOfGenreAfterSoftDelete() {
        final GenreId beforeDeleteId = existingSciFi.getId();
        final Genre deleted = existingSciFi.softDelete();

        assertThat(deleted).isSameAs(existingSciFi);
        assertThat(existingSciFi.isDeleted()).isTrue();
        assertThat(existingSciFi.getLifecycle().deletedAt()).isNotNull();
        assertThat(existingSciFi.getId()).isEqualTo(beforeDeleteId);
        assertThat(existingSciFi.getName()).isEqualTo(SCI_FI);
        assertThat(existingSciFi.getDescription()).isEqualTo("desc");
    }

    @Test
    void testThatRenameDoesNotChangeId() {
        final GenreName newGenreName = new GenreName("Science Fiction");
        final Genre renamed = createdSciFi.rename(newGenreName);

        assertThat(renamed.getName()).isEqualTo(newGenreName);
        assertThat(renamed.getId()).isEqualTo(createdGenreId);
        assertThat(renamed).isSameAs(createdSciFi);
    }

    @Test
    void describeWithNullClearsTheDescription() {
        assertThat(createdSciFi.describe(null).getDescription()).isNull();
    }
}
