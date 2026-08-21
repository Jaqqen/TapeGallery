package com.jaqqen.tapeshub.controller.dto.tape;

import com.jaqqen.tapeshub.domain.tape.TapePattern;
import jakarta.validation.Valid;

/**
 * Body of PATCH /api/tapes/{id}. Every field is optional; only non-null fields
 * are applied onto the existing tape.
 */
public record PatchTapeRequest(
        String title,
        String subtitle,
        String year,
        String genre,
        String duration,
        String rating,
        String description,
        @Valid TapeColorsDto colors,
        TapePattern pattern
) {
}
