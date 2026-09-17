package com.jaqqen.tapeshub.tape;

import com.jaqqen.tapeshub.support.ApiIntegrationTest;
import com.jaqqen.tapeshub.support.TapeRequests;
import com.jaqqen.tapeshub.tape.app.dto.TapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import com.jaqqen.tapeshub.tape.domain.TapePattern;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Drives {@code /api/tapes} over real HTTP against a real Postgres - the one path nothing else
 * exercises: {@code TapeControllerTest} mocks the service, {@code JpaTapeRepositoryTest} skips the
 * web layer entirely.
 */
class TapeApiIT extends ApiIntegrationTest {

    private static final ParameterizedTypeReference<List<TapeResponse>> TAPE_LIST =
        new ParameterizedTypeReference<>() {
        };

    @Test
    void createReturns201WithALocationHeaderAndTheStoredTape() {
        UUID genreId = createGenre("Action");

        TapeResponse body = authedPost("/api/tapes", TapeRequests.tape(genreId, "Neon Nights"))

            .expectStatus().isCreated()
            .expectHeader().valueMatches("Location", ".+/api/tapes/[0-9a-fA-F-]{36}$")
            .expectBody(TapeResponse.class)
            .returnResult()
            .getResponseBody();

        TapeResponse tape = Objects.requireNonNull(body);
        // The nested genre is the full record, not a bare id - a client renders the tape without a
        // second round trip.
        assertThat(tape.genre().id()).isEqualTo(genreId);
        assertThat(tape.genre().name()).isEqualTo("Action");
        assertThat(tape.title()).isEqualTo("Neon Nights");
        assertThat(tape.createdAt()).isEqualTo(tape.modifiedAt());
        assertThat(tape.deletedAt()).isNull();
    }

    @Test
    void theCreatedTapeIsReadableAtItsLocation() {
        UUID genreId = createGenre("Action");
        String location = authedPost("/api/tapes", TapeRequests.tape(genreId, "Neon Nights"))
            .expectStatus().isCreated()
            .expectHeader().exists("Location")
            .returnResult(TapeResponse.class)
            .getResponseHeaders()
            .getFirst("Location");

        // Follow the header verbatim: it must be usable, not merely well-formed.
        authedGet(java.net.URI.create(Objects.requireNonNull(location)))
            .expectStatus().isOk()
            .expectBody(TapeResponse.class)
            .value(tape -> assertThat(Objects.requireNonNull(tape).title()).isEqualTo("Neon Nights"));
    }

    @Test
    void listReturnsEveryLiveTapeSortedByTitle() {
        UUID genreId = createGenre("Action");
        createTape(genreId, "Velvet Thunder");
        createTape(genreId, "Neon Nights");
        createTape(genreId, "Chrome Horizon");

        List<TapeResponse> tapes = Objects.requireNonNull(
            authedGet("/api/tapes")
                .expectStatus().isOk()
                .expectBody(TAPE_LIST)
                .returnResult()
                .getResponseBody());

        assertThat(tapes).extracting(TapeResponse::title)
            .containsExactly("Chrome Horizon", "Neon Nights", "Velvet Thunder");
    }

    @Test
    void putReplacesEveryFieldAndMovesModifiedAt() {
        UUID genreId = createGenre("Action");
        UUID tapeId = createTape(genreId, "Neon Nights");

        TapeRequest replacement = new TapeRequest(
            "Neon Nights Redux", "Recut", LocalDate.of(1990, 1, 1), genreId,
            9_000_000, TapeRequests.RETROWAVE_COLORS, TapePattern.WAVES);

        TapeResponse tape = Objects.requireNonNull(
            authedPut("/api/tapes/{id}", replacement, tapeId)
                .expectStatus().isOk()
                .expectBody(TapeResponse.class)
                .returnResult()
                .getResponseBody());

        assertThat(tape.title()).isEqualTo("Neon Nights Redux");
        assertThat(tape.duration()).isEqualTo(9_000_000);
        assertThat(tape.pattern()).isEqualTo(TapePattern.WAVES);
        assertThat(tape.createdAt()).isNotEqualTo(tape.modifiedAt());
        assertThat(tape.modifiedAt()).isAfter(tape.createdAt());
    }

    @Test
    void patchChangesOnlyTheFieldsNamedInTheBody() {
        UUID genreId = createGenre("Action");
        UUID tapeId = createTape(genreId, "Neon Nights");

        TapeResponse tape = Objects.requireNonNull(
            authedPatch("/api/tapes/{id}", """
                {"title": "Neon Nights II"}""", tapeId)
                .expectStatus().isOk()
                .expectBody(TapeResponse.class)
                .returnResult()
                .getResponseBody());

        assertThat(tape.title()).isEqualTo("Neon Nights II");
        assertThat(tape.subtitle()).isEqualTo("A Retrowave Journey");
        assertThat(tape.duration()).isEqualTo(5_400_000);
        assertThat(tape.pattern()).isEqualTo(TapePattern.RETRO_BLOCKS);
        assertThat(tape.colors()).isEqualTo(TapeRequests.RETROWAVE_COLORS);
    }

    @Test
    void patchCanReclassifyTheTapeToAnotherGenre() {
        UUID actionId = createGenre("Action");
        UUID horrorId = createGenre("Horror");
        UUID tapeId = createTape(actionId, "Neon Nights");

        TapeResponse tape = Objects.requireNonNull(
            authedPatch("/api/tapes/{id}", "{\"genreId\": \"" + horrorId + "\"}", tapeId)
                .expectStatus().isOk()
                .expectBody(TapeResponse.class)
                .returnResult()
                .getResponseBody());

        assertThat(tape.genre().id()).isEqualTo(horrorId);
        assertThat(tape.genre().name()).isEqualTo("Horror");
    }

    @Test
    void deleteReturns204AndTheTapeIsGoneFromGetAndFromList() {
        UUID genreId = createGenre("Action");
        UUID tapeId = createTape(genreId, "Neon Nights");

        authedDelete("/api/tapes/{id}", tapeId)
            .expectStatus().isNoContent();

        authedGet("/api/tapes/{id}", tapeId)
            .expectStatus().isNotFound();

        List<TapeResponse> tapes = Objects.requireNonNull(
            authedGet("/api/tapes")
                .expectStatus().isOk()
                .expectBody(TAPE_LIST)
                .returnResult()
                .getResponseBody());

        assertThat(tapes).extracting(TapeResponse::id).doesNotContain(tapeId);
    }

    @Test
    void deletingTheSameTapeTwiceReturns404() {
        UUID genreId = createGenre("Action");
        UUID tapeId = createTape(genreId, "Neon Nights");

        authedDelete("/api/tapes/{id}", tapeId).expectStatus().isNoContent();
        authedDelete("/api/tapes/{id}", tapeId)
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Tape not found");
    }

    @Test
    void anUnknownTapeIdReturns404AsProblemJson() {
        authedGet("/api/tapes/{id}", UUID.randomUUID())
            .expectStatus().isNotFound()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Tape not found");
    }

    @Test
    void aPathSegmentThatIsNotAUuidReturns400() {
        authedGet("/api/tapes/{id}", "neon-nights")
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Invalid request")
            .jsonPath("$.detail").isEqualTo("'neon-nights' is not a valid identifier");
    }

    @Test
    void creatingATapeWithAGenreThatDoesNotExistReturns422() {
        UUID unknownGenre = UUID.randomUUID();

        // 422, not 404: the URL is fine, it is the body that names something that does not exist.
        authedPost("/api/tapes", TapeRequests.tape(unknownGenre, "Neon Nights"))
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Unknown genre");
    }

    @Test
    void theTapePatternRoundTripsAsKebabCase() {
        UUID genreId = createGenre("Action");

        String location = authedPost("/api/tapes",
            TapeRequests.tape(genreId, "Neon Nights", TapePattern.RETRO_BLOCKS))
            .expectStatus().isCreated()
            // web-portal expects "retro-blocks"; the constant name RETRO_BLOCKS must never reach it.
            .expectBody()
            .jsonPath("$.pattern").isEqualTo("retro-blocks")
            .returnResult()
            .getResponseHeaders()
            .getFirst("Location");

        authedGet(java.net.URI.create(Objects.requireNonNull(location)))
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.pattern").isEqualTo("retro-blocks");
    }

    @Test
    void anUnknownPatternValueReturns400() {
        UUID genreId = createGenre("Action");
        String body = """
            {
              "title": "Neon Nights",
              "releaseDate": "1987-06-12",
              "genreId": "%s",
              "duration": 5400000,
              "colors": {"primary": "#ff006e", "secondary": "#8338ec", "accent": "#3a86ff", "label": "#fb5607"},
              "pattern": "polka-dots"
            }""".formatted(genreId);

        // An HttpMessageNotReadableException, not bean validation: the enum cannot be constructed
        // from an unknown value at all, so this fails before @Valid ever runs.
        authedPost("/api/tapes", body)
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Malformed request body");
    }

    @Test
    void aBodyThatFailsValidationReturns400WithASortedErrorList() {
        UUID genreId = createGenre("Action");
        String body = """
            {
              "title": "",
              "releaseDate": "1987-06-12",
              "genreId": "%s",
              "duration": -1,
              "colors": {"primary": "not-a-colour", "secondary": "#8338ec", "accent": "#3a86ff", "label": "#fb5607"},
              "pattern": "waves"
            }""".formatted(genreId);

        // ApiExceptionHandler sorts the field errors alphabetically - nothing else asserts that.
        authedPost("/api/tapes", body)
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Validation failed")
            .jsonPath("$.errors[0]").value((String error) -> assertThat(error).startsWith("colors.primary "))
            .jsonPath("$.errors[1]").value((String error) -> assertThat(error).startsWith("duration "))
            .jsonPath("$.errors[2]").value((String error) -> assertThat(error).startsWith("title "));
    }

    @Test
    void lifecycleStampsSurviveTheRoundTripThroughPostgres() {
        UUID genreId = createGenre("Action");
        UUID tapeId = createTape(genreId, "Neon Nights");

        java.time.Instant createdAtOnCreate = Objects.requireNonNull(
            authedGet("/api/tapes/{id}", tapeId)
                .expectStatus().isOk()
                .expectBody(TapeResponse.class)
                .returnResult()
                .getResponseBody())
            .createdAt();

        // Lifecycle truncates every stamp to microseconds because that is all timestamptz stores;
        // this proves the truncation matches all the way out to the JSON layer.
        authedGet("/api/tapes/{id}", tapeId)
            .expectStatus().isOk()
            .expectBody(TapeResponse.class)
            .value(tape -> assertThat(Objects.requireNonNull(tape).createdAt()).isEqualTo(createdAtOnCreate));
    }
}
