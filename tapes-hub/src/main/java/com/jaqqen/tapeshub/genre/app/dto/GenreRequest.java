package com.jaqqen.tapeshub.genre.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body of POST /api/genres and PUT /api/genres/{id}. Both carry the same fields - a genre is
 * identified by its path, never by its body - so one record serves both.
 */
public record GenreRequest(
    @NotBlank @Size(max = 64) String name,
    String description
) {
}
