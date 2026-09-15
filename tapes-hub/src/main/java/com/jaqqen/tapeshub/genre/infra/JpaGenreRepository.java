package com.jaqqen.tapeshub.genre.infra;

import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.domain.Genre;
import com.jaqqen.tapeshub.genre.domain.GenreRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JpaGenreRepository implements GenreRepository {

    private final GenreEntityRepository entities;

    JpaGenreRepository(GenreEntityRepository entities) {
        this.entities = entities;
    }

    @Override
    public List<Genre> findAll() {
        return entities.findAllByDeletedAtIsNull(Sort.by("name")).stream().map(GenreMapper::toDomain).toList();
    }

    @Override
    public Optional<Genre> findById(GenreId id) {
        return entities.findByIdAndDeletedAtIsNull(id.value()).map(GenreMapper::toDomain);
    }

    @Override
    public Optional<Genre> findByName(String name) {
        return entities.findByNameAndDeletedAtIsNull(name).map(GenreMapper::toDomain);
    }

    @Override
    public List<Genre> findAllByIds(Collection<GenreId> ids) {
        List<UUID> keys = ids.stream().map(GenreId::value).toList();
        return entities.findAllByIdInAndDeletedAtIsNull(keys).stream().map(GenreMapper::toDomain).toList();
    }

    @Override
    public Genre save(Genre genre) {
        entities.save(GenreMapper.toEntity(genre));
        return genre;
    }

    @Override
    public boolean softDeleteById(GenreId id) {
        Optional<GenreEntity> row = entities.findByIdAndDeletedAtIsNull(id.value());
        if (row.isEmpty()) {
            return false;
        }

        save(GenreMapper.toDomain(row.get()).softDelete());
        return true;
    }
}
