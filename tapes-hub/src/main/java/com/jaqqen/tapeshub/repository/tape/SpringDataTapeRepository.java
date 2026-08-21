package com.jaqqen.tapeshub.repository.tape;

import com.jaqqen.tapeshub.repository.tape.model.TapeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data's view of the tapes table. Queries go by slug because that is the
 * identifier {@link TapeRepository} speaks in; the UUID primary key stays internal.
 */
interface SpringDataTapeRepository extends JpaRepository<TapeEntity, UUID> {

    List<TapeEntity> findAllByOrderBySlugAsc();

    Optional<TapeEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    long deleteBySlug(String slug);
}
