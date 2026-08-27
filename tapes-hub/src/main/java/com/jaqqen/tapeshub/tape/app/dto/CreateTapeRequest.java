package com.jaqqen.tapeshub.tape.app.dto;

import com.jaqqen.tapeshub.tape.domain.TapePattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Body of POST /api/tapes. Carries no identifier: the id is minted by the server.
 * {@code genre} is the id of an existing, separately-managed genre.
 */
public record CreateTapeRequest(
        @NotBlank String title,
        String subtitle,
        @NotNull LocalDate releaseDate,
        @NotNull UUID genre,
        @Positive int duration,
        @NotNull @Valid TapeColorsDto colors,
        @NotNull TapePattern pattern
) {
}
