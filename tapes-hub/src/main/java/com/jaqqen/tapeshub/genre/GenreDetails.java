package com.jaqqen.tapeshub.genre;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record GenreDetails(UUID id, String name, @Nullable String description) {
}
