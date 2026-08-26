package com.jaqqen.tapeshub.tape.infra;

import com.jaqqen.tapeshub.tape.domain.TapeGenre;
import com.jaqqen.tapeshub.tape.domain.TapeGenreRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class JpaTapeGenreRepository implements TapeGenreRepository {
    private final TapeGenreEntityRepository ger;

    public JpaTapeGenreRepository(TapeGenreEntityRepository ger) {
        this.ger = ger;
    }

    @Override
    public TapeGenre findById(UUID id) {
        return ger.findById(id)
            .map(TapeMapper::toTapeGenre)
            .orElseThrow(() -> new IllegalArgumentException("Unknown genre id: " + id));
    }
}
