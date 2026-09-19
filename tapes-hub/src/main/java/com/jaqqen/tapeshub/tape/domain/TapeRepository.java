package com.jaqqen.tapeshub.tape.domain;

import org.jmolecules.ddd.types.Repository;

import java.util.List;
import java.util.Optional;

public interface TapeRepository extends Repository<Tape, TapeId> {

    List<Tape> findAll();

    Optional<Tape> findById(TapeId id);

    Tape save(Tape tape);

    /** @return {@code false} if there was no such tape. */
    boolean deleteById(TapeId id);
}
