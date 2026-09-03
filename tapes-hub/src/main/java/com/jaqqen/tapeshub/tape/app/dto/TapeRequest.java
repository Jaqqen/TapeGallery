package com.jaqqen.tapeshub.tape.app.dto;

import com.jaqqen.tapeshub.tape.domain.TapePattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Body of {@code POST /api/tapes} and {@code PUT /api/tapes/{id}}.
 *
 * <p>
 *     Used to CREATE and REPLACE with same null-safety measures. The id is auto-generated through its
 *     domain class {@link com.jaqqen.tapeshub.tape.domain.Tape} and replace receives the id through a
 *     separate parameter
 * </p>
 * <p>{@link #genreId} must name a genre that already exists.</p>
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
