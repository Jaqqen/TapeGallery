package com.jaqqen.tapeshub.genre;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A read-only view of a genre, and the wire format of {@code /api/genres}.
 *
 * <p>Carries plain types rather than the aggregate's value objects: it crosses both the module
 * boundary and the HTTP boundary, and neither consumer should have to know how a {@code Genre}
 * is built.
 */
public record GenreDetails(UUID id, String name, @Nullable String description) {
}
