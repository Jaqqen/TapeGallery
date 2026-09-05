package com.jaqqen.tapeshub.genre.presentation;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.app.GenreServiceImpl;
import com.jaqqen.tapeshub.genre.domain.GenreInUseException;
import com.jaqqen.tapeshub.genre.domain.GenreNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The HTTP edge of the genre module: status codes, the {@code Location} header, and the
 * {@code application/problem+json} bodies its {@link GenreExceptionHandler} produces. The service is
 * mocked - what a genre <em>is</em> belongs to the tests further in.
 *
 * <p>Filters are off: the chain in {@code SecurityConfig} is the config module's, and
 * {@code SecurityConfigTest} is where it is checked.
 */
@WebMvcTest(GenreController.class)
@AutoConfigureMockMvc(addFilters = false)
class GenreControllerTest {

    private static final UUID ID = UUID.fromString("8e17b20c-0e19-4c68-9eba-f5d5e9e9688d");
    private static final GenreDetails HORROR = new GenreDetails(ID, "Horror", "Built to frighten.");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GenreServiceImpl service;

    @Test
    void listReturnsEveryGenre() throws Exception {
        when(service.list()).thenReturn(List.of(HORROR));

        mvc.perform(get("/api/genres"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(ID.toString()))
            .andExpect(jsonPath("$[0].name").value("Horror"))
            .andExpect(jsonPath("$[0].description").value("Built to frighten."));
    }

    @Test
    void getReturnsOneGenre() throws Exception {
        when(service.get(ID)).thenReturn(HORROR);

        mvc.perform(get("/api/genres/{id}", ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Horror"));
    }

    @Test
    void getOfAnUnknownGenreIs404() throws Exception {
        when(service.get(ID)).thenThrow(new GenreNotFoundException(new GenreId(ID)));

        mvc.perform(get("/api/genres/{id}", ID))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Genre not found"))
            .andExpect(jsonPath("$.detail").value("No genre with id '%s'".formatted(ID)));
    }

    @Test
    void getWithSomethingThatIsNotAUuidIs400() throws Exception {
        // Handled by the application-wide ApiExceptionHandler, not this module's.
        mvc.perform(get("/api/genres/{id}", "horror"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Invalid request"))
            .andExpect(jsonPath("$.detail").value("'horror' is not a valid identifier"));
    }

    @Test
    void createReturns201WithALocationHeader() throws Exception {
        when(service.create(any())).thenReturn(HORROR);

        mvc.perform(post("/api/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Horror","description":"Built to frighten."}"""))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/api/genres/" + ID))
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void createAcceptsAGenreWithoutADescription() throws Exception {
        when(service.create(any())).thenReturn(new GenreDetails(ID, "Horror", null));

        mvc.perform(post("/api/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Horror"}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void createWithABlankNameIs400AndNeverReachesTheService() throws Exception {
        mvc.perform(post("/api/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"  "}"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.startsWith("name ")));

        verify(service, never()).create(any());
    }

    @Test
    void createWithATooLongNameIs400() throws Exception {
        mvc.perform(post("/api/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + "x".repeat(65) + "\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void createWithAMalformedBodyIs400() throws Exception {
        mvc.perform(post("/api/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Malformed request body"));
    }

    @Test
    void replaceReturnsTheUpdatedGenre() throws Exception {
        GenreDetails renamed = new GenreDetails(ID, "Horror & Suspense", null);
        when(service.replace(eq(ID), any())).thenReturn(renamed);

        mvc.perform(put("/api/genres/{id}", ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Horror & Suspense"}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Horror & Suspense"));
    }

    @Test
    void replaceOfAnUnknownGenreIs404() throws Exception {
        when(service.replace(eq(ID), any())).thenThrow(new GenreNotFoundException(new GenreId(ID)));

        mvc.perform(put("/api/genres/{id}", ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Horror"}"""))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204WithNoBody() throws Exception {
        mvc.perform(delete("/api/genres/{id}", ID))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verify(service).delete(ID);
    }

    @Test
    void deleteOfAnUnknownGenreIs404() throws Exception {
        doThrow(new GenreNotFoundException(new GenreId(ID))).when(service).delete(ID);

        mvc.perform(delete("/api/genres/{id}", ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Genre not found"));
    }

    @Test
    void deleteOfAGenreStillInUseIs409() throws Exception {
        doThrow(new GenreInUseException(new GenreId(ID))).when(service).delete(ID);

        // 409, not 404 or 500: the request is well-formed, the genre's current state refuses it.
        mvc.perform(delete("/api/genres/{id}", ID))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Genre in use"))
            .andExpect(jsonPath("$.detail")
                .value("Genre '%s' is still in use and cannot be deleted".formatted(ID)));
    }
}
