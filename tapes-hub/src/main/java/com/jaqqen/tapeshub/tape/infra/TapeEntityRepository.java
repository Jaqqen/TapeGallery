package com.jaqqen.tapeshub.tape.infra;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface TapeEntityRepository extends JpaRepository<TapeEntity, UUID> {

    List<TapeEntity> findAllByDeletedAtIsNull(Sort sort);

    Optional<TapeEntity> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByGenreIdAndDeletedAtIsNull(UUID genreId);

}
