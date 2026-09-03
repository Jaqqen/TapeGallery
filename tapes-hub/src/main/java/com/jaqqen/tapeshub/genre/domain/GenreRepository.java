package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import org.jmolecules.ddd.types.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The genre module's persistence port. Declared here, in the domain ring, and implemented in
 * {@code genre.infra} - so the domain says what it needs and JPA stays on the outside.
 */
public interface GenreRepository extends Repository<Genre, GenreId> {

    List<Genre> findAll();

    Optional<Genre> findById(GenreId id);

    Optional<Genre> findByName(String name);

    List<Genre> findAllByIds(Collection<GenreId> ids);

    Genre save(Genre genre);

    /** @return {@code false} if there was no such genre, so callers can turn that into a 404. */
    boolean deleteById(GenreId id);
}
