package com.jaqqen.tapeshub.repository.tape;

import com.jaqqen.tapeshub.domain.tape.Tape;

import java.util.List;
import java.util.Optional;

/**
 * Storage seam for tapes. The in-memory implementation is the only one today;
 * a persistent one can drop in without touching the service or controller.
 */
public interface TapeRepository {

    List<Tape> findAll();

    Optional<Tape> findById(String id);

    boolean existsById(String id);

    Tape save(Tape tape);

    /** @return true when a tape was removed, false when the id was unknown. */
    boolean deleteById(String id);
}
