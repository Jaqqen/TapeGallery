package com.jaqqen.tapeshub.repository.tape;

import com.jaqqen.tapeshub.repository.tape.model.TapeEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Storage seam for tapes. Everything keys off the tape's id.
 */
public interface TapeRepository extends JpaRepository<TapeEntity, UUID> {

    /** {@code findAll} makes no ordering promise, so the listing order lives here. */
    List<TapeEntity> findAllByOrderByTitleAsc();

    @Transactional
    default boolean removeById(UUID id) {
        if (!existsById(id)) {
            return false;
        }
        deleteById(id);
        return true;
    }
}
