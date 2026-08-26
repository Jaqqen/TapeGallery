package com.jaqqen.tapeshub.tape.domain;

import java.util.UUID;

/** Read-only lookup of reference-data genres by id. */
public interface TapeGenreRepository {
    TapeGenre findById(UUID id);
}
