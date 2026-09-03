package com.jaqqen.tapeshub.genre.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Body of {@code POST /api/tapes} and {@code PUT /api/tapes/{id}}.
 * <p>
 *     Used to CREATE and REPLACE with same null-safety measures. The id is auto-generated through its
 *     domain class {@link com.jaqqen.tapeshub.genre.domain.Genre} and replace receives the id through a
 *     separate parameter
 * </p>
 */
public record GenreRequest(
    @NotBlank @Size(max = 64) String name,
    @Nullable String description
) {
}
