package com.jaqqen.tapeshub.repository;

import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.repository.tape.TapeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test fake for {@link TapeRepository}: keeps tapes in a map keyed by id, mirroring the
 * semantics of the Postgres implementation. Lets the service tests exercise patch
 * merging without starting a database.
 */
public class InMemoryTapeRepository implements TapeRepository {

    private final Map<UUID, Tape> tapes = new ConcurrentHashMap<>();

    @Override
    public List<Tape> findAll() {
        return tapes.values().stream()
                .sorted(Comparator.comparing(Tape::title))
                .toList();
    }

    @Override
    public Optional<Tape> findById(UUID id) {
        return Optional.ofNullable(tapes.get(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return tapes.containsKey(id);
    }

    @Override
    public Tape save(Tape tape) {
        tapes.put(tape.id(), tape);
        return tape;
    }

    @Override
    public boolean deleteById(UUID id) {
        return tapes.remove(id) != null;
    }
}
