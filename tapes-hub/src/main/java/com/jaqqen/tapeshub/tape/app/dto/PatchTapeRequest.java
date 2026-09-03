package com.jaqqen.tapeshub.tape.app.dto;

import com.jaqqen.tapeshub.tape.domain.TapePattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Body of PATCH /api/tapes/{id}. Every field is optional and only the ones present are applied,
 * which is why {@code duration} is boxed - {@code 0} and "not sent" have to stay distinguishable.
 */
public record PatchTapeRequest(
    String title,
    String subtitle,
    LocalDate releaseDate,
    UUID genreId,
    @Positive Integer duration,
    @Valid TapeColorsDto colors,
    TapePattern pattern
) {
}
