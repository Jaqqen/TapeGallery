package com.jaqqen.tapeshub.tape.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface TapeEntityRepository extends JpaRepository<TapeEntity, UUID> {
}
