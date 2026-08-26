package com.jaqqen.tapeshub.tape.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TapeGenreEntityRepository extends JpaRepository<TapeGenreEntity, UUID> {
}
