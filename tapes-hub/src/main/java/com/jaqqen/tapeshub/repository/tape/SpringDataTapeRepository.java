package com.jaqqen.tapeshub.repository.tape;

import com.jaqqen.tapeshub.repository.tape.model.TapeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data's view of the tapes table. Lookups by id come from {@link JpaRepository}
 * itself; the only thing worth declaring is a stable ordering for the collection.
 */
interface SpringDataTapeRepository extends JpaRepository<TapeEntity, UUID> {

    List<TapeEntity> findAllByOrderByTitleAsc();
}
