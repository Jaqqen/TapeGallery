package com.jaqqen.tapeshub.controller.dto.tape;

import com.jaqqen.tapeshub.domain.tape.TapePattern;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Body of POST /api/tapes. Carries no identifier: the id is minted by the server.
 */
public record CreateTapeRequest(
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
