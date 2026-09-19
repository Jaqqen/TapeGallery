package com.jaqqen.tapeshub.genre.infra;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface GenreEntityRepository extends JpaRepository<GenreEntity, UUID> {

    List<GenreEntity> findAllByDeletedAtIsNull(Sort sort);

    Optional<GenreEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<GenreEntity> findByNameAndDeletedAtIsNull(String name);

    List<GenreEntity> findAllByIdInAndDeletedAtIsNull(Collection<UUID> ids);
}
