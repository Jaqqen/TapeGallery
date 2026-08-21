package com.jaqqen.tapeshub.repository;

import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.repository.tape.TapeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test fake for {@link TapeRepository}: keeps tapes in a map, mirroring the semantics
 * of the Postgres implementation. Lets the service tests exercise slug derivation and
 * patch merging without starting a database.
 */
public class InMemoryTapeRepository implements TapeRepository {

    private final Map<String, Tape> tapes = new ConcurrentHashMap<>();

    @Override
    public List<Tape> findAll() {
        return tapes.values().stream()
                .sorted(Comparator.comparing(Tape::id))
                .toList();
    }

    @Override
    public Optional<Tape> findById(String id) {
        return Optional.ofNullable(tapes.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return tapes.containsKey(id);
    }

    @Override
    public Tape save(Tape tape) {
        tapes.put(tape.id(), tape);
        return tape;
    }

    @Override
    public boolean deleteById(String id) {
        return tapes.remove(id) != null;
    }
}
