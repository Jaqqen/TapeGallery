package com.jaqqen.tapeshub.tape;

import com.jaqqen.tapeshub.support.ApiIntegrationTest;
import com.jaqqen.tapeshub.support.TapeRequests;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The seam between the two modules: a tape only ever holds a {@code GenreId}, and everything a
 * client sees about the genre it points at is resolved live through {@code GenreService} at read
 * time. {@code TapeServiceTest} only sees that resolution mocked; this is the real thing.
 */
class TapeGenreCrossModuleIT extends ApiIntegrationTest {

    private static final ParameterizedTypeReference<List<TapeResponse>> TAPE_LIST =
        new ParameterizedTypeReference<>() {
        };

    @Test
    void aTapeCarriesTheFullDetailsOfTheGenreItPointsAt() {
        UUID genreId = createGenre("Action");
        UUID tapeId = createTape(genreId, "Neon Nights");

        authedGet("/api/tapes/{id}", tapeId)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.genre.id").isEqualTo(genreId.toString())
            .jsonPath("$.genre.name").isEqualTo("Action")
            .jsonPath("$.genre.createdAt").exists();
    }

    @Test
    void renamingAGenreIsVisibleOnEveryTapeThatUsesIt() {
        UUID genreId = createGenre("Action");
        UUID tapeId = createTape(genreId, "Neon Nights");

        authedPut("/api/genres/{id}", TapeRequests.genre("Action Movies"), genreId)
            .expectStatus().isOk();

        // TapeService resolves the genre live through GenreService rather than snapshotting it at
        // creation time, so a rename elsewhere shows up here without touching the tape.
        authedGet("/api/tapes/{id}", tapeId)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.genre.name").isEqualTo("Action Movies");
    }

    @Test
    void listResolvesEveryGenreInOneBatch() {
        UUID actionId = createGenre("Action");
        UUID horrorId = createGenre("Horror");
        createTape(actionId, "Neon Nights");
        createTape(horrorId, "Blood Moon");
        createTape(actionId, "Chrome Horizon");

        List<TapeResponse> tapes = Objects.requireNonNull(
            authedGet("/api/tapes")
                .expectStatus().isOk()
                .expectBody(TAPE_LIST)
                .returnResult()
                .getResponseBody());

        assertThat(tapes).hasSize(3);
        assertThat(tapes).filteredOn(tape -> tape.title().equals("Blood Moon"))
            .extracting(tape -> tape.genre().name())
            .containsExactly("Horror");
        assertThat(tapes).filteredOn(tape -> !tape.title().equals("Blood Moon"))
            .extracting(tape -> tape.genre().name())
            .containsOnly("Action");
    }
}
