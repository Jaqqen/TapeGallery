package com.jaqqen.tapeshub.repository.tape;

import com.jaqqen.tapeshub.domain.tape.Tape;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage seam for tapes. Everything keys off the tape's id.
 */
public interface TapeRepository {

    List<Tape> findAll();

    Optional<Tape> findById(UUID id);

    boolean existsById(UUID id);

    Tape save(Tape tape);

    /** @return true when a tape was removed, false when the id was unknown. */
    boolean deleteById(UUID id);
}
