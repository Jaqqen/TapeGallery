package com.jaqqen.tapeshub.genre.app;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.GenreService;
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

@Service
@Transactional
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    public GenreServiceImpl(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Transactional(readOnly = true)
    public List<GenreDetails> list() {
        return genreRepository.findAll().stream().map(GenreServiceImpl::detailsOf).toList();
    }

    @Transactional(readOnly = true)
    public GenreDetails get(UUID id) {
        GenreId genreId = new GenreId(id);
        return genreRepository.findById(genreId)
            .map(GenreServiceImpl::detailsOf)
            .orElseThrow(() -> new GenreNotFoundException(genreId));
    }

    public GenreDetails create(GenreRequest request) {
        Genre genre = Genre.create(new GenreName(request.name()), request.description());
        return detailsOf(genreRepository.save(genre));
    }

    public GenreDetails replace(UUID id, GenreRequest request) {
        GenreId genreId = new GenreId(id);
        Genre genre = genreRepository.findById(genreId)
            .orElseThrow(() -> new GenreNotFoundException(genreId))
            .rename(new GenreName(request.name()))
            .describe(request.description());
        return detailsOf(genreRepository.save(genre));
    }

    public void delete(UUID id) {
        GenreId genreId = new GenreId(id);
        if (!genreRepository.deleteById(genreId)) {
            throw new GenreNotFoundException(genreId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GenreDetails> findById(GenreId id) {
        return genreRepository.findById(id).map(GenreServiceImpl::detailsOf);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GenreDetails> findByName(String name) {
        return genreRepository.findByName(name).map(GenreServiceImpl::detailsOf);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<GenreId, GenreDetails> findAllByIds(Collection<GenreId> ids) {
        return genreRepository.findAllByIds(ids).stream()
            .collect(Collectors.toMap(Genre::getId, GenreServiceImpl::detailsOf,
                (first, second) -> first));
    }

    private static GenreDetails detailsOf(Genre genre) {
        return new GenreDetails(genre.getId().value(), genre.getName().value(), genre.getDescription());
    }
}
