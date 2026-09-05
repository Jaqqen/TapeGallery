package com.jaqqen.tapeshub.tape.presentation;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.tape.app.TapeService;
import com.jaqqen.tapeshub.tape.app.UnknownGenreException;
import com.jaqqen.tapeshub.tape.app.dto.TapeColorsDto;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import com.jaqqen.tapeshub.tape.domain.TapeId;
import com.jaqqen.tapeshub.tape.domain.TapeNotFoundException;
import com.jaqqen.tapeshub.tape.domain.TapePattern;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The HTTP edge of the tape module: the wire format, and the two failures it has to tell apart -
 * a tape that does not exist (404) and a body naming a genre that does not (422).
 */
@WebMvcTest(TapeController.class)
@AutoConfigureMockMvc(addFilters = false)
class TapeControllerTest {

    private static final UUID TAPE_ID = UUID.fromString("3f2504e0-4f89-41d3-9a0c-0305e82c3301");
    private static final UUID GENRE_ID = UUID.fromString("8e17b20c-0e19-4c68-9eba-f5d5e9e9688d");
    private static final GenreDetails ACTION = new GenreDetails(GENRE_ID, "Action", "Chases and stunts.");

    private static final TapeResponse NEON_NIGHTS = new TapeResponse(
        TAPE_ID, "NEON NIGHTS", "The City Never Sleeps", LocalDate.of(1987, 1, 1), ACTION,
        6_840_000, new TapeColorsDto("#ff006e", "#8338ec", "#ffbe0b", "#1a1a2e"), TapePattern.RETRO_BLOCKS);

    private static final String VALID_BODY = """
        {
          "title": "NEON NIGHTS",
          "subtitle": "The City Never Sleeps",
          "releaseDate": "1987-01-01",
          "genreId": "8e17b20c-0e19-4c68-9eba-f5d5e9e9688d",
          "duration": 6840000,
          "colors": {"primary": "#ff006e", "secondary": "#8338ec", "accent": "#ffbe0b", "label": "#1a1a2e"},
          "pattern": "stripes"
        }""";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TapeService service;

    @Test
    void listReturnsEveryTapeWithItsGenreExpanded() throws Exception {
        when(service.list()).thenReturn(List.of(NEON_NIGHTS));

        mvc.perform(get("/api/tapes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(TAPE_ID.toString()))
            .andExpect(jsonPath("$[0].title").value("NEON NIGHTS"))
            // Nested, not a bare id - the client renders a tape without a second round trip.
            .andExpect(jsonPath("$[0].genre.id").value(GENRE_ID.toString()))
            .andExpect(jsonPath("$[0].genre.name").value("Action"));
    }

    @Test
    void patternIsWrittenAsItsKebabCaseWireValue() throws Exception {
        when(service.get(TAPE_ID)).thenReturn(NEON_NIGHTS);

        // web-portal expects "retro-blocks"; the constant name RETRO_BLOCKS must never reach it.
        mvc.perform(get("/api/tapes/{id}", TAPE_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pattern").value("retro-blocks"));
    }

    @Test
    void getReturnsTheWholeTape() throws Exception {
        when(service.get(TAPE_ID)).thenReturn(NEON_NIGHTS);

        mvc.perform(get("/api/tapes/{id}", TAPE_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subtitle").value("The City Never Sleeps"))
            .andExpect(jsonPath("$.releaseDate").value("1987-01-01"))
            .andExpect(jsonPath("$.duration").value(6_840_000))
            .andExpect(jsonPath("$.colors.primary").value("#ff006e"))
            .andExpect(jsonPath("$.colors.label").value("#1a1a2e"));
    }

    @Test
    void getOfAnUnknownTapeIs404() throws Exception {
        when(service.get(TAPE_ID)).thenThrow(new TapeNotFoundException(new TapeId(TAPE_ID)));

        mvc.perform(get("/api/tapes/{id}", TAPE_ID))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Tape not found"))
            .andExpect(jsonPath("$.detail").value("No tape with id '%s'".formatted(TAPE_ID)));
    }

    @Test
    void getWithSomethingThatIsNotAUuidIs400() throws Exception {
        mvc.perform(get("/api/tapes/{id}", "neon-nights"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Invalid request"))
            .andExpect(jsonPath("$.detail").value("'neon-nights' is not a valid identifier"));
    }

    @Test
    void createReturns201WithALocationHeader() throws Exception {
        when(service.create(any())).thenReturn(NEON_NIGHTS);

        mvc.perform(post("/api/tapes").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/api/tapes/" + TAPE_ID))
            .andExpect(jsonPath("$.id").value(TAPE_ID.toString()));
    }

    @Test
    void createWithAnUnknownGenreIs422() throws Exception {
        when(service.create(any())).thenThrow(new UnknownGenreException(new GenreId(GENRE_ID)));

        // 422, not 404: the URL is fine, the body is what names something that does not exist.
        mvc.perform(post("/api/tapes").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.title").value("Unknown genre"))
            .andExpect(jsonPath("$.detail").value("No genre with id '%s'".formatted(GENRE_ID)));
    }

    @Test
    void createWithABlankTitleIs400AndNeverReachesTheService() throws Exception {
        mvc.perform(post("/api/tapes").contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY.replace("\"NEON NIGHTS\"", "\"  \"")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0]").value(Matchers.startsWith("title ")));

        verify(service, never()).create(any());
    }

    @Test
    void createWithANonPositiveDurationIs400() throws Exception {
        mvc.perform(post("/api/tapes").contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY.replace("6840000", "0")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0]").value(Matchers.startsWith("duration ")));
    }

    @Test
    void createWithoutAGenreIdIs400() throws Exception {
        mvc.perform(post("/api/tapes").contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY.replace("\"8e17b20c-0e19-4c68-9eba-f5d5e9e9688d\"", "null")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0]").value(Matchers.startsWith("genreId ")));
    }

    @Test
    void createWithAColourThatIsNotHexIs400() throws Exception {
        // Caught by the DTO's @Pattern, so it is a 400 with a field name rather than the domain's
        // IllegalArgumentException surfacing further in.
        mvc.perform(post("/api/tapes").contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY.replace("\"#ff006e\"", "\"red\"")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0]")
                .value(Matchers.containsString("must be a hex colour such as #ff006e")));
    }

    @Test
    void createWithAnUnknownPatternIs400() throws Exception {
        mvc.perform(post("/api/tapes").contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY.replace("\"stripes\"", "\"plaid\"")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Malformed request body"));
    }

    @Test
    void createWithAMalformedBodyIs400() throws Exception {
        mvc.perform(post("/api/tapes").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Malformed request body"));
    }

    @Test
    void replaceReturnsTheUpdatedTape() throws Exception {
        when(service.replace(eq(TAPE_ID), any())).thenReturn(NEON_NIGHTS);

        mvc.perform(put("/api/tapes/{id}", TAPE_ID)
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("NEON NIGHTS"));
    }

    @Test
    void replaceOfAnUnknownTapeIs404() throws Exception {
        when(service.replace(eq(TAPE_ID), any()))
            .thenThrow(new TapeNotFoundException(new TapeId(TAPE_ID)));

        mvc.perform(put("/api/tapes/{id}", TAPE_ID)
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isNotFound());
    }

    @Test
    void replaceStillRequiresAWholeBody() throws Exception {
        mvc.perform(put("/api/tapes/{id}", TAPE_ID)
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"title": "NEON NIGHTS"}"""))
            .andExpect(status().isBadRequest())
            // Rejected while the body is being read, not by bean validation: TapeRequest declares
            // duration as a primitive int, and Jackson refuses to map an absent field onto one. So
            // an incomplete PUT is a "Malformed request body" rather than a field-by-field
            // "Validation failed" - blunter, but still a 400 that never reaches the service.
            .andExpect(jsonPath("$.title").value("Malformed request body"))
            .andExpect(jsonPath("$.detail").value(Matchers.containsString("into type `int`")));

        verify(service, never()).replace(any(), any());
    }

    @Test
    void patchAcceptsASingleField() throws Exception {
        when(service.patch(eq(TAPE_ID), any())).thenReturn(NEON_NIGHTS);

        // The difference from PUT: everything except the id is optional.
        mvc.perform(patch("/api/tapes/{id}", TAPE_ID)
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"title": "NEON NIGHTS II"}"""))
            .andExpect(status().isOk());

        verify(service).patch(eq(TAPE_ID), any());
    }

    @Test
    void patchAcceptsAnEmptyBody() throws Exception {
        when(service.patch(eq(TAPE_ID), any())).thenReturn(NEON_NIGHTS);

        mvc.perform(patch("/api/tapes/{id}", TAPE_ID)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    void patchStillValidatesTheFieldsItWasGiven() throws Exception {
        mvc.perform(patch("/api/tapes/{id}", TAPE_ID)
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"duration": -1}"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0]").value(Matchers.startsWith("duration ")));

        verify(service, never()).patch(any(), any());
    }

    @Test
    void patchWithAColourThatIsNotHexIs400() throws Exception {
        mvc.perform(patch("/api/tapes/{id}", TAPE_ID)
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"colors": {"primary": "red", "secondary": "#8338ec", "accent": "#ffbe0b", "label": "#1a1a2e"}}"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void patchOntoAnUnknownGenreIs422() throws Exception {
        when(service.patch(eq(TAPE_ID), any()))
            .thenThrow(new UnknownGenreException(new GenreId(GENRE_ID)));

        mvc.perform(patch("/api/tapes/{id}", TAPE_ID)
                .contentType(MediaType.APPLICATION_JSON).content("""
                    {"genreId": "8e17b20c-0e19-4c68-9eba-f5d5e9e9688d"}"""))
            .andExpect(status().isUnprocessableContent())
            .andExpect(jsonPath("$.title").value("Unknown genre"));
    }

    @Test
    void deleteReturns204WithNoBody() throws Exception {
        mvc.perform(delete("/api/tapes/{id}", TAPE_ID))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verify(service).delete(TAPE_ID);
    }

    @Test
    void deleteOfAnUnknownTapeIs404() throws Exception {
        doThrow(new TapeNotFoundException(new TapeId(TAPE_ID))).when(service).delete(TAPE_ID);

        mvc.perform(delete("/api/tapes/{id}", TAPE_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Tape not found"));
    }
}
