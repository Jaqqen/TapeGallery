package com.jaqqen.tapeshub.tape.domain;

import com.jaqqen.tapeshub.tape.domain.exception.TapeNotFoundException;

import java.util.List;
import java.util.UUID;

public interface TapeRepository {
    List<Tape> findAll();
    Tape save(Tape tape);
    Tape findById(UUID id) throws TapeNotFoundException;
    boolean existsById(UUID id);
    boolean deleteById(UUID id);
}
