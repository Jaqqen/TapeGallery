package com.jaqqen.tapeshub.genre;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * The read-only slice of the genre module that other modules may use.
 *
 * <p>This is the entire contract between {@code tape} and {@code genre}: a tape resolves the genre
 * it points at, and can neither create nor mutate one. Writes stay behind {@code /api/genres}.
 */
public interface Genres {

    Optional<GenreDetails> findById(GenreId id);

    Optional<GenreDetails> findByName(String name);

    /**
     * Resolves many genres in one query, so rendering a list of tapes does not fan out into one
     * lookup per tape. Ids with no matching genre are simply absent from the result.
     */
    Map<GenreId, GenreDetails> findAllByIds(Collection<GenreId> ids);
}
