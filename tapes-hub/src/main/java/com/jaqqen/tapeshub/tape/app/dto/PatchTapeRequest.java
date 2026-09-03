package com.jaqqen.tapeshub.tape.app.dto;

import com.jaqqen.tapeshub.tape.domain.TapePattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Body of PATCH /api/tapes/{id}. Every field is optional and only the ones present are applied,
 * which is why {@code duration} is boxed - {@code 0} and "not sent" have to stay distinguishable.
 */
public record PatchTapeRequest(
    @Nullable String title,
    @Nullable String subtitle,
    @Nullable LocalDate releaseDate,
    @Nullable UUID genreId,
    @Positive @Nullable Integer duration,
    @Valid @Nullable TapeColorsDto colors,
    @Nullable TapePattern pattern
) {
}
