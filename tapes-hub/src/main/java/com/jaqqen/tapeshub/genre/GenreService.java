package com.jaqqen.tapeshub.genre;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface GenreService {

    Optional<GenreDetails> findById(GenreId id);

    Optional<GenreDetails> findByName(String name);

    /**
     * Resolves many genres in one query, so rendering a list of tapes does not fan out into one
     * lookup per tape. Ids with no matching genre are simply absent from the result.
     */
    Map<GenreId, GenreDetails> findAllByIds(Collection<GenreId> ids);
}
