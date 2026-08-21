package com.jaqqen.tapeshub.controller.dto.tape;

import com.jaqqen.tapeshub.domain.tape.TapePattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Body of POST /api/tapes. {@code id} is optional - it is derived from the title when absent.
 */
public record CreateTapeRequest(
        String id,
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
