package com.jaqqen.tapeshub.tape.infra;

import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.GenreUsage;
import org.springframework.stereotype.Component;

/**
 * The {@code Genre} used in {@link TapeEntity}.
 */
@Component
class TapeGenreUsage implements GenreUsage {

    private final TapeEntityRepository tapes;

    TapeGenreUsage(TapeEntityRepository tapes) {
        this.tapes = tapes;
    }

    @Override
    public boolean isInUse(GenreId id) {
        return tapes.existsByGenreId(id.value());
    }
}
