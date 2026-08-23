package com.jaqqen.tapeshub.controller.dto.tape;

import com.jaqqen.tapeshub.domain.tape.TapePattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Body of PUT /api/tapes/{id} - a full replacement, so every field is required. The
 * tape is identified by the path alone.
 */
public record UpdateTapeRequest(
        @NotBlank String title,
        String subtitle,
        @NotBlank String year,
        @NotBlank String genre,
        @NotBlank String duration,
        @NotBlank String rating,
        @NotBlank String description,
        @NotNull @Valid TapeColorsDto colors,
        @NotNull TapePattern pattern
) {
}
