package com.jaqqen.tapeshub.tape.app.dto;

import com.jaqqen.tapeshub.tape.domain.TapePattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Body of POST /api/tapes and PUT /api/tapes/{id}.
 *
 * <p>One record for both: a create carries no identifier because the server mints it, and a
 * replacement is identified by its path - so the two bodies are the same shape, and every field is
 * required in each. {@code genreId} must name a genre that already exists.
 */
public record TapeRequest(
    @NotBlank String title,
    String subtitle,
    @NotNull LocalDate releaseDate,
    @NotNull UUID genreId,
    @Positive int duration,
    @NotNull @Valid TapeColorsDto colors,
    @NotNull TapePattern pattern
) {
}
