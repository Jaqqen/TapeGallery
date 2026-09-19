package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import org.jmolecules.ddd.types.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenreRepository extends Repository<Genre, GenreId> {

    List<Genre> findAll();

    Optional<Genre> findById(GenreId id);

    Optional<Genre> findByName(String name);

    List<Genre> findAllByIds(Collection<GenreId> ids);

    Genre save(Genre genre);

    /** @return {@code false} if there was no such genre, {@code true} if successfully soft deleted*/
    boolean softDeleteById(GenreId id);
}
