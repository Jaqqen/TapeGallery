package com.jaqqen.tapeshub.tape.domain;

import org.jmolecules.ddd.types.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The tape module's persistence port. Declared here, in the domain ring, and implemented in
 * {@code tape.infra} - so the domain says what it needs and JPA stays on the outside.
 */
public interface TapeRepository extends Repository<Tape, TapeId> {

    List<Tape> findAll();

    Optional<Tape> findById(TapeId id);

    Tape save(Tape tape);

    /** @return {@code false} if there was no such tape, so callers can turn that into a 404. */
    boolean deleteById(TapeId id);
}
