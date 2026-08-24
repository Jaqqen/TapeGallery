package com.jaqqen.tapeshub.repository.tape;

import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.repository.tape.model.TapeEntity;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage seam for tapes. Everything keys off the tape's id.
 */
@Primary
public interface TapeRepository extends JpaRepository<TapeEntity, UUID> {
    @Transactional
    default boolean removeById(UUID id) {
        if (!existsById(id)) {
            return false;
        }
        deleteById(id);
        return true;
    }
}
