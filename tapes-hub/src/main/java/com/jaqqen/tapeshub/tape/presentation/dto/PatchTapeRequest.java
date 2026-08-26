package com.jaqqen.tapeshub.tape.presentation.dto;

import com.jaqqen.tapeshub.tape.domain.TapePattern;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Body of PATCH /api/tapes/{id}. Every field is optional; only non-null fields
 * are applied onto the existing tape. {@code genre} is the id of an existing,
 * separately-managed genre.
 */
public record PatchTapeRequest(
        String title,
        String subtitle,
        LocalDate releaseDate,
        UUID genre,
        Integer duration,
        @Valid TapeColorsDto colors,
        TapePattern pattern
) {
}
