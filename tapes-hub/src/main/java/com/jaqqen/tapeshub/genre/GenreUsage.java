package com.jaqqen.tapeshub.genre;

/**
 * Defines the usage of a {@link com.jaqqen.tapeshub.genre.domain.Genre} outside of its own domain.
 */
public interface GenreUsage {

    /** @return {@code true} if another entity still references this genre. */
    boolean isInUse(GenreId id);
}
