package com.jaqqen.tapeshub.genre.infra;

import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.domain.Genre;
import com.jaqqen.tapeshub.genre.domain.GenreInUseException;
import com.jaqqen.tapeshub.genre.domain.GenreRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The JPA adapter behind {@link GenreRepository}. */
@Repository
class JpaGenreRepository implements GenreRepository {

    private final GenreEntityRepository entities;

    JpaGenreRepository(GenreEntityRepository entities) {
        this.entities = entities;
    }

    @Override
    public List<Genre> findAll() {
        return entities.findAll(Sort.by("name")).stream().map(GenreMapper::toDomain).toList();
    }

    @Override
    public Optional<Genre> findById(GenreId id) {
        return entities.findById(id.value()).map(GenreMapper::toDomain);
    }

    @Override
    public Optional<Genre> findByName(String name) {
        return entities.findByName(name).map(GenreMapper::toDomain);
    }

    @Override
    public List<Genre> findAllByIds(Collection<GenreId> ids) {
        List<UUID> keys = ids.stream().map(GenreId::value).toList();
        return entities.findAllById(keys).stream().map(GenreMapper::toDomain).toList();
    }

    @Override
    public Genre save(Genre genre) {
        entities.save(GenreMapper.toEntity(genre));
        return genre;
    }

    @Override
    public boolean deleteById(GenreId id) {
        if (!entities.existsById(id.value())) {
            return false;
        }
        entities.deleteById(id.value());
        try {
            // Push the delete to the database now. Left to the commit, a tape's foreign key would
            // blow up long after this adapter is off the stack, where nothing can name what failed.
            entities.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new GenreInUseException(id);
        }
        return true;
    }
}
