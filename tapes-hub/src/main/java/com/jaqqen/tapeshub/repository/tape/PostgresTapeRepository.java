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
        return tapes.findAllByOrderBySlugAsc().stream().map(TapeEntity::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tape> findById(String id) {
        return tapes.findBySlug(id).map(TapeEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        return tapes.existsBySlug(id);
    }

    /**
     * Upserts on the slug. An existing tape is updated in place so that its surrogate
     * id survives - {@code replace} and {@code patch} both round-trip through here.
     */
    @Override
    public Tape save(Tape tape) {
        TapeEntity entity = tapes.findBySlug(tape.id())
                .map(existing -> {
                    existing.apply(tape);
                    return existing;
                })
                .orElseGet(() -> TapeEntity.fromDomain(UUID.randomUUID(), tape));
        return tapes.save(entity).toDomain();
    }

    @Override
    public boolean deleteById(String id) {
        return tapes.deleteBySlug(id) > 0;
    }
}
