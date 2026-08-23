package com.jaqqen.tapeshub.controller;

import com.jaqqen.tapeshub.TapeFixtures;
import com.jaqqen.tapeshub.controller.dto.tape.PatchTapeRequest;
import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.exception.TapeNotFoundException;
import com.jaqqen.tapeshub.service.TapeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TapeController.class)
@AutoConfigureMockMvc(addFilters = false)
class TapeControllerTest {

    private static final UUID ID = TapeFixtures.NEON_NIGHTS_ID;
    private static final UUID UNKNOWN = UUID.fromString("99999999-9999-4999-8999-999999999999");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TapeService service;

    @Test
    void listsTapes() throws Exception {
        given(service.findAll()).willReturn(java.util.List.of(TapeFixtures.neonNights()));

        mockMvc.perform(get("/api/tapes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID.toString()))
                .andExpect(jsonPath("$[0].pattern").value("stripes"))
                .andExpect(jsonPath("$[0].colors.primary").value("#ff006e"));
    }

    @Test
    void getsATapeById() throws Exception {
        given(service.findById(ID)).willReturn(TapeFixtures.neonNights());

        mockMvc.perform(get("/api/tapes/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("NEON NIGHTS"));
    }

    @Test
    void getReturns404ForAnUnknownTape() throws Exception {
        given(service.findById(UNKNOWN)).willThrow(new TapeNotFoundException(UNKNOWN));

        mockMvc.perform(get("/api/tapes/" + UNKNOWN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Tape not found"));
    }

    @Test
    void getRejectsAPathThatIsNotAnId() throws Exception {
        mockMvc.perform(get("/api/tapes/neon-nights"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void createsATapeAndReturnsItsLocation() throws Exception {
        given(service.create(any())).willReturn(TapeFixtures.neonNights());

        mockMvc.perform(post("/api/tapes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TapeFixtures.NEON_NIGHTS_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/tapes/" + ID))
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void createRejectsABlankTitle() throws Exception {
        mockMvc.perform(post("/api/tapes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TapeFixtures.NEON_NIGHTS_JSON.replace("\"NEON NIGHTS\"", "\"\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.startsWith("title ")));
    }

    @Test
    void createRejectsANonHexColour() throws Exception {
        mockMvc.perform(post("/api/tapes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TapeFixtures.NEON_NIGHTS_JSON.replace("#ff006e", "hot-pink")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.startsWith("colors.primary ")));
    }

    @Test
    void createRejectsAnUnknownPattern() throws Exception {
        mockMvc.perform(post("/api/tapes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TapeFixtures.NEON_NIGHTS_JSON.replace("\"stripes\"", "\"polka-dots\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request body"));
    }

    @Test
    void replacesATape() throws Exception {
        given(service.replace(eq(ID), any())).willReturn(TapeFixtures.neonNights());

        mockMvc.perform(put("/api/tapes/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TapeFixtures.NEON_NIGHTS_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void replaceReturns404ForAnUnknownTape() throws Exception {
        given(service.replace(eq(UNKNOWN), any())).willThrow(new TapeNotFoundException(UNKNOWN));

        mockMvc.perform(put("/api/tapes/" + UNKNOWN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TapeFixtures.NEON_NIGHTS_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchesASingleField() throws Exception {
        Tape existing = TapeFixtures.neonNights();
        Tape patched = new Tape(existing.id(), existing.title(), existing.subtitle(),
                existing.year(), existing.genre(), existing.duration(), "PG-13", existing.description(),
                existing.colors(), existing.pattern());
        given(service.patch(eq(ID), any())).willReturn(patched);

        mockMvc.perform(patch("/api/tapes/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":\"PG-13\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value("PG-13"))
                .andExpect(jsonPath("$.title").value("NEON NIGHTS"));

        then(service).should().patch(eq(ID),
                eq(new PatchTapeRequest(null, null, null, null, null, "PG-13", null, null, null)));
    }

    @Test
    void deletesATape() throws Exception {
        doNothing().when(service).delete(ID);

        mockMvc.perform(delete("/api/tapes/" + ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturns404ForAnUnknownTape() throws Exception {
        willThrow(new TapeNotFoundException(UNKNOWN)).given(service).delete(UNKNOWN);

        mockMvc.perform(delete("/api/tapes/" + UNKNOWN))
                .andExpect(status().isNotFound());
    }
}
