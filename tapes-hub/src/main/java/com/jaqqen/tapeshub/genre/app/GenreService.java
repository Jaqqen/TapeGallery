package com.jaqqen.tapeshub.genre.app;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.Genres;
import com.jaqqen.tapeshub.genre.app.dto.GenreRequest;
import com.jaqqen.tapeshub.genre.domain.Genre;
import com.jaqqen.tapeshub.genre.domain.GenreName;
import com.jaqqen.tapeshub.genre.domain.GenreNotFoundException;
import com.jaqqen.tapeshub.genre.domain.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The genre module's only application service.
 *
 * <p>It implements {@link Genres} rather than delegating to a second read-only bean: {@code Genres}
 * is the narrow view other modules are given, and this is the class that can answer it.
 */
@Service
@Transactional
public class GenreService implements Genres {

    private final GenreRepository genres;

    public GenreService(GenreRepository genres) {
        this.genres = genres;
    }

    @Transactional(readOnly = true)
    public List<GenreDetails> list() {
        return genres.findAll().stream().map(GenreService::detailsOf).toList();
    }

    @Transactional(readOnly = true)
    public GenreDetails get(UUID id) {
        GenreId genreId = new GenreId(id);
        return genres.findById(genreId)
            .map(GenreService::detailsOf)
            .orElseThrow(() -> new GenreNotFoundException(genreId));
    }

    public GenreDetails create(GenreRequest request) {
        Genre genre = Genre.create(new GenreName(request.name()), request.description());
        return detailsOf(genres.save(genre));
    }

    public GenreDetails replace(UUID id, GenreRequest request) {
        GenreId genreId = new GenreId(id);
        Genre genre = genres.findById(genreId)
            .orElseThrow(() -> new GenreNotFoundException(genreId))
            .rename(new GenreName(request.name()))
            .describe(request.description());
        return detailsOf(genres.save(genre));
    }

    public void delete(UUID id) {
        GenreId genreId = new GenreId(id);
        if (!genres.deleteById(genreId)) {
            throw new GenreNotFoundException(genreId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GenreDetails> findById(GenreId id) {
        return genres.findById(id).map(GenreService::detailsOf);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GenreDetails> findByName(String name) {
        return genres.findByName(name).map(GenreService::detailsOf);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<GenreId, GenreDetails> findAllByIds(Collection<GenreId> ids) {
        return genres.findAllByIds(ids).stream()
            .collect(Collectors.toMap(Genre::getId, GenreService::detailsOf,
                (first, second) -> first));
    }

    private static GenreDetails detailsOf(Genre genre) {
        return new GenreDetails(genre.getId().value(), genre.getName().value(), genre.getDescription());
    }
}
