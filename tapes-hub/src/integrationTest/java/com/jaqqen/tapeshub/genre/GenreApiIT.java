package com.jaqqen.tapeshub.genre;

import com.jaqqen.tapeshub.support.ApiIntegrationTest;
import com.jaqqen.tapeshub.support.TapeRequests;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@code /api/genres} with Postgres.
 */
class GenreApiIT extends ApiIntegrationTest {

    private static final ParameterizedTypeReference<List<GenreDetails>> GENRE_LIST =
        new ParameterizedTypeReference<>() {
        };

    @Test
    void createReturns201WithALocationHeader() {
        GenreDetails genre = Objects.requireNonNull(
            authedPost("/api/genres", TapeRequests.genre("Horror"))
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".+/api/genres/[0-9a-fA-F-]{36}$")
                .expectBody(GenreDetails.class)
                .returnResult()
                .getResponseBody());

        assertThat(genre.name()).isEqualTo("Horror");
        assertThat(genre.createdAt()).isEqualTo(genre.modifiedAt());
        assertThat(genre.deletedAt()).isNull();
    }

    @Test
    void theCreatedGenreIsReadableAtItsLocation() {
        String location = authedPost("/api/genres", TapeRequests.genre("Horror"))
            .expectStatus().isCreated()
            .returnResult(GenreDetails.class)
            .getResponseHeaders()
            .getFirst("Location");

        authedGet(java.net.URI.create(Objects.requireNonNull(location)))
            .expectStatus().isOk()
            .expectBody(GenreDetails.class)
            .value(genre -> assertThat(Objects.requireNonNull(genre).name()).isEqualTo("Horror"));
    }

    @Test
    void listReturnsEveryLiveGenre() {
        createGenre("Horror");
        createGenre("Action");

        List<GenreDetails> genres = Objects.requireNonNull(
            authedGet("/api/genres")
                .expectStatus().isOk()
                .expectBody(GENRE_LIST)
                .returnResult()
                .getResponseBody());

        assertThat(genres).extracting(GenreDetails::name).containsExactlyInAnyOrder("Action", "Horror");
    }

    @Test
    void putRenamesAndRedescribesTheGenre() {
        UUID genreId = createGenre("Horror");

        GenreDetails genre = Objects.requireNonNull(
            authedPut("/api/genres/{id}", TapeRequests.genre("Slasher"), genreId)
                .expectStatus().isOk()
                .expectBody(GenreDetails.class)
                .returnResult()
                .getResponseBody());

        assertThat(genre.name()).isEqualTo("Slasher");
        assertThat(genre.description()).isEqualTo("Slasher tapes.");
    }

    @Test
    void deleteReturns204AndTheGenreDisappearsFromGetAndList() {
        UUID genreId = createGenre("Horror");

        authedDelete("/api/genres/{id}", genreId).expectStatus().isNoContent();
        authedGet("/api/genres/{id}", genreId).expectStatus().isNotFound();

        List<GenreDetails> genres = Objects.requireNonNull(
            authedGet("/api/genres")
                .expectStatus().isOk()
                .expectBody(GENRE_LIST)
                .returnResult()
                .getResponseBody());

        assertThat(genres).extracting(GenreDetails::id).doesNotContain(genreId);
    }

    @Test
    void deletingAGenreThatALiveTapeUsesReturns409() {
        UUID genreId = createGenre("Action");
        createTape(genreId, "Neon Nights");

        authedDelete("/api/genres/{id}", genreId)
            .expectStatus().isEqualTo(org.springframework.http.HttpStatus.CONFLICT)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Genre in use");
    }

    @Test
    void aGenreWhoseOnlyTapeHasBeenDeletedCanItselfBeDeleted() {
        UUID genreId = createGenre("Action");
        UUID tapeId = createTape(genreId, "Neon Nights");

        authedDelete("/api/tapes/{id}", tapeId).expectStatus().isNoContent();

        authedDelete("/api/genres/{id}", genreId).expectStatus().isNoContent();
    }

    @Test
    void aNameLongerThan64CharactersReturns400() {
        String name = "N".repeat(65);

        authedPost("/api/genres", TapeRequests.genre(name))
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Validation failed")
            .jsonPath("$.errors[0]").value((String error) -> assertThat(error).startsWith("name "));
    }

    @Test
    void aBlankNameReturns400WithErrors() {
        authedPost("/api/genres", TapeRequests.genre("   "))
            .expectStatus().isBadRequest()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Validation failed")
            .jsonPath("$.errors[0]").value((String error) -> assertThat(error).startsWith("name "));
    }

    @Test
    void deletingTheSameGenreTwiceReturns404() {
        UUID genreId = createGenre("Horror");

        authedDelete("/api/genres/{id}", genreId).expectStatus().isNoContent();
        authedDelete("/api/genres/{id}", genreId)
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Genre not found");
    }

    @Test
    void anUnknownGenreIdReturns404AsProblemJson() {
        authedGet("/api/genres/{id}", UUID.randomUUID())
            .expectStatus().isNotFound()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Genre not found");
    }
}
