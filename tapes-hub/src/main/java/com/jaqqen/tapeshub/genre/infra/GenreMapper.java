package com.jaqqen.tapeshub.genre.infra;

import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.domain.Genre;
import com.jaqqen.tapeshub.genre.domain.GenreName;

/** Translates between the {@link Genre} domain and db entity. */

final class GenreMapper {

    private GenreMapper() {
    }

    static GenreEntity toEntity(Genre genre) {
        return new GenreEntity(genre.getId().value(), genre.getName().value(), genre.getDescription());
    }

    static Genre toDomain(GenreEntity entity) {
        return Genre.existing(
            new GenreId(entity.getId()),
            new GenreName(entity.getName()),
            entity.getDescription());
    }
}
