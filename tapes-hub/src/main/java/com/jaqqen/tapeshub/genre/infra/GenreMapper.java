package com.jaqqen.tapeshub.genre.infra;

import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.domain.Genre;
import com.jaqqen.tapeshub.genre.domain.GenreName;
import com.jaqqen.tapeshub.shared.Lifecycle;

/** Translates between the {@link Genre} domain and db entity. */

final class GenreMapper {

    private GenreMapper() {
    }

    static GenreEntity toEntity(Genre genre) {
        Lifecycle lifecycle = genre.getLifecycle();
        return new GenreEntity(
            genre.getId().value(),
            genre.getName().value(),
            genre.getDescription(),
            lifecycle.createdAt(),
            lifecycle.modifiedAt(),
            lifecycle.deletedAt());
    }

    static Genre toDomain(GenreEntity entity) {
        return Genre.existing(
            new GenreId(entity.getId()),
            new GenreName(entity.getName()),
            entity.getDescription(),
            new Lifecycle(entity.getCreatedAt(), entity.getModifiedAt(), entity.getDeletedAt()));
    }
}
