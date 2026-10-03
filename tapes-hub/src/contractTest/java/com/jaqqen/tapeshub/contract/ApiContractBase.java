package com.jaqqen.tapeshub.contract;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.app.GenreServiceImpl;
import com.jaqqen.tapeshub.genre.presentation.GenreController;
import com.jaqqen.tapeshub.tape.app.TapeService;
import com.jaqqen.tapeshub.tape.app.UnknownGenreException;
import com.jaqqen.tapeshub.tape.app.dto.TapeColorsDto;
import com.jaqqen.tapeshub.tape.app.dto.TapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import com.jaqqen.tapeshub.tape.domain.TapePattern;
import com.jaqqen.tapeshub.tape.presentation.TapeController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * What the generated contract tests run against.
 *
 * <p>A web slice rather than a standalone MockMvc setup, deliberately: standalone builds its own
 * message converters, so dates would serialise as numbers there and as ISO strings in the real
 * application - and a contract that passes against the wrong serialiser is worse than none.
 *
 * <p>Filters are off, and the injected {@code MockMvc} is what makes that stick: RestAssured's
 * webAppContextSetup builds its own instance off the context and reinstates the whole filter chain.
 * Contracts describe the shape of a response, not who may ask for one - {@code SecurityConfigIT}
 * owns that.
 */
@WebMvcTest(controllers = {TapeController.class, GenreController.class})
@AutoConfigureMockMvc(addFilters = false)
public abstract class ApiContractBase {

    /** Fixed so the contract files can state the exact bytes a client will receive. */
    private static final UUID GENRE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TAPE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TAPE_ID_2 = UUID.fromString("33333333-3333-3333-3333-333333333333");
    /** Names no genre, so a create carrying it is how the 422 contract is reached. */
    private static final UUID UNKNOWN_GENRE_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant MODIFIED_AT = Instant.parse("2026-01-02T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TapeService tapeService;

    @MockitoBean
    private GenreServiceImpl genreService;

    private static GenreDetails genre() {
        return new GenreDetails(GENRE_ID, "Sci-Fi", "Spaceships and synthesizers", CREATED_AT, MODIFIED_AT, null);
    }

    private static TapeResponse tape() {
        return new TapeResponse(
            TAPE_ID_2,
            "Blooming Desert",
            "Manager's budget",
            LocalDate.of(2001, 11, 12),
            genre(),
            5_540_000,
            new TapeColorsDto("#ff2d95", "#00f0ff", "#ffe156", "#1a1a2e"),
            TapePattern.DIAMONDS,
            CREATED_AT,
            MODIFIED_AT,
            null);
    }

    private static TapeResponse tape2() {
        return new TapeResponse(
            TAPE_ID,
            "Neon Nights",
            "Director's Cut",
            LocalDate.of(1987, 1, 1),
            genre(),
            6_840_000,
            new TapeColorsDto("#ff2d95", "#00f0ff", "#ffe156", "#1a1a2e"),
            TapePattern.RETRO_BLOCKS,
            CREATED_AT,
            MODIFIED_AT,
            null);
    }

    @BeforeEach
    void setUp() {
        BDDMockito.given(tapeService.list()).willReturn(List.of(tape(), tape2()));
        BDDMockito.given(tapeService.create(ArgumentMatchers.any(TapeRequest.class))).willReturn(tape2());
        BDDMockito.given(tapeService.create(ArgumentMatchers.argThat(request ->
                UNKNOWN_GENRE_ID.equals(request.genreId()))))
            .willThrow(new UnknownGenreException(new GenreId(UNKNOWN_GENRE_ID)));
        BDDMockito.given(genreService.list()).willReturn(List.of(genre()));

        RestAssuredMockMvc.mockMvc(mockMvc);
    }
}
