package com.jaqqen.tapeshub.genre.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface GenreEntityRepository extends JpaRepository<GenreEntity, UUID> {

    Optional<GenreEntity> findByName(String name);
}
