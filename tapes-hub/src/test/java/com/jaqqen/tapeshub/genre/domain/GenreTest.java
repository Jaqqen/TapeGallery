package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenreTest {

    private static final GenreName SCI_FI = new GenreName("Sci-Fi");

    @Test
    void createMintsItsOwnIdentity() {
        Genre first = Genre.create(SCI_FI, "Speculative technology and futures.");
        Genre second = Genre.create(SCI_FI, null);

        assertThat(first.getId()).isNotNull();
        // Identity is minted, never passed in - so two genres of the same name are still two genres.
        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    void createKeepsWhatItWasGiven() {
        Genre genre = Genre.create(SCI_FI, "Speculative technology and futures.");

        assertThat(genre.getName()).isEqualTo(SCI_FI);
        assertThat(genre.getDescription()).isEqualTo("Speculative technology and futures.");
    }

    @Test
    void createAllowsNoDescription() {
        assertThat(Genre.create(SCI_FI, null).getDescription()).isNull();
    }

    @Test
    void existingCarriesThePersistedIdentity() {
        GenreId id = GenreId.newId();

        Genre genre = Genre.existing(id, SCI_FI, "desc");

        assertThat(genre.getId()).isEqualTo(id);
    }

    @Test
    void renameChangesTheNameButNotTheIdentity() {
        Genre genre = Genre.create(SCI_FI, "desc");
        GenreId id = genre.getId();

        Genre renamed = genre.rename(new GenreName("Science Fiction"));

        assertThat(renamed.getName()).isEqualTo(new GenreName("Science Fiction"));
        assertThat(renamed.getId()).isEqualTo(id);
        // The operations mutate and return this, so callers can chain - they are not copies.
        assertThat(renamed).isSameAs(genre);
    }

    @Test
    void describeReplacesTheDescription() {
        Genre genre = Genre.create(SCI_FI, "old");

        assertThat(genre.describe("new").getDescription()).isEqualTo("new");
    }

    @Test
    void describeWithNullClearsTheDescription() {
        Genre genre = Genre.create(SCI_FI, "old");

        // GenreServiceImpl.replace() relies on this: a PUT without a description has to blank it out,
        // not silently keep the previous one.
        assertThat(genre.describe(null).getDescription()).isNull();
    }
}
