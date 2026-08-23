package com.jaqqen.tapeshub.repository.tape;

import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.repository.tape.model.TapeEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Postgres-backed {@link TapeRepository}. Maps between the domain records and
 * {@link TapeEntity} so no JPA type escapes this package.
 */
@Repository
@Transactional
public class PostgresTapeRepository implements TapeRepository {

    private final SpringDataTapeRepository tapes;

    public PostgresTapeRepository(SpringDataTapeRepository tapes) {
        this.tapes = tapes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tape> findAll() {
        return tapes.findAllByOrderByTitleAsc().stream().map(TapeEntity::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tape> findById(UUID id) {
        return tapes.findById(id).map(TapeEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return tapes.existsById(id);
    }

    /** Upserts on the id, so an existing tape is updated in place rather than duplicated. */
    @Override
    public Tape save(Tape tape) {
        TapeEntity entity = tapes.findById(tape.id())
                .map(existing -> {
                    existing.apply(tape);
                    return existing;
                })
                .orElseGet(() -> TapeEntity.fromDomain(tape));
        return tapes.save(entity).toDomain();
    }

    @Override
    public boolean deleteById(UUID id) {
        if (!tapes.existsById(id)) {
            return false;
        }
        tapes.deleteById(id);
        return true;
    }
}
