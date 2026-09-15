package com.jaqqen.tapeshub.genre;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * What every genre endpoint returns, and the only shape of a genre other modules ever see.
 *
 * <p>{@link #deletedAt} is always {@code null} here: a deleted genre is filtered out before it can be
 * turned into details. It is carried anyway so the field means the same thing on the wire as it does
 * in storage, rather than being a hole a client has to know about.
 */
public record GenreDetails(
    UUID id,
    String name,
    @Nullable String description,
    Instant createdAt,
    Instant modifiedAt,
    @Nullable Instant deletedAt
) {
}
