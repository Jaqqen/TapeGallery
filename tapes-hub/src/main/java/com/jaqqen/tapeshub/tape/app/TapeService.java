package com.jaqqen.tapeshub.tape.app;

import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapeDuration;
import com.jaqqen.tapeshub.tape.domain.TapeGenreRepository;
import com.jaqqen.tapeshub.tape.domain.TapeId;
import com.jaqqen.tapeshub.tape.domain.TapeRepository;
import com.jaqqen.tapeshub.tape.domain.TapeTitle;
import com.jaqqen.tapeshub.tape.presentation.dto.CreateTapeRequest;
import com.jaqqen.tapeshub.tape.presentation.dto.PatchTapeRequest;
import com.jaqqen.tapeshub.tape.presentation.dto.TapeColorsDto;
import com.jaqqen.tapeshub.tape.presentation.dto.UpdateTapeRequest;
import com.jaqqen.tapeshub.tape.presentation.exception.TapeNotFoundRuntimeException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TapeService {

    private final TapeRepository repository;
    private final TapeGenreRepository genreRepository;

    public TapeService(TapeRepository repository, TapeGenreRepository genreRepository) {
        this.repository = repository;
        this.genreRepository = genreRepository;
    }

    public List<Tape> findAll() {
        return repository.findAll();
    }

    public Tape findById(UUID id) {
        return getOrThrow(id);
    }

    public Tape create(CreateTapeRequest request) {
        Tape tape = new Tape(
            new TapeTitle(request.title()),
            subtitleOf(request.subtitle()),
            request.releaseDate(),
            genreRepository.findById(request.genre()),
            new TapeDuration(request.duration()),
            request.colors().toDomain(),
            request.pattern()
        );
        return repository.save(tape);
    }

    public Tape replace(UUID id, UpdateTapeRequest request) {
        if (!repository.existsById(id)) {
            throw new TapeNotFoundRuntimeException(id);
        }
        Tape tape = Tape.builder()
            .id(new TapeId(id))
            .title(new TapeTitle(request.title()))
            .subtitle(subtitleOf(request.subtitle()))
            .releaseDate(request.releaseDate())
            .genre(genreRepository.findById(request.genre()))
            .duration(new TapeDuration(request.duration()))
            .colors(request.colors().toDomain())
            .pattern(request.pattern())
            .build();
        return repository.save(tape);
    }

    public Tape patch(UUID id, PatchTapeRequest request) {
        Tape existing = getOrThrow(id);
        TapeColorsDto colors = request.colors();
        Tape tape = Tape.builder()
            .id(existing.getId())
            .title(request.title() != null ? new TapeTitle(request.title()) : existing.getTitle())
            .subtitle(request.subtitle() != null ? subtitleOf(request.subtitle()) : existing.getSubtitle())
            .releaseDate(orExisting(request.releaseDate(), existing.getReleaseDate()))
            .genre(request.genre() != null ? genreRepository.findById(request.genre()) : existing.getGenre())
            .duration(request.duration() != null ? new TapeDuration(request.duration()) : existing.getDuration())
            .colors(colors != null ? colors.toDomain() : existing.getColors())
            .pattern(orExisting(request.pattern(), existing.getPattern()))
            .build();
        return repository.save(tape);
    }

    public void delete(UUID id) {
        if (!repository.deleteById(id)) {
            throw new TapeNotFoundRuntimeException(id);
        }
    }

    private Tape getOrThrow(UUID id) {
        try {
            return repository.findById(id);
        } catch (com.jaqqen.tapeshub.tape.domain.exception.TapeNotFoundException e) {
            throw new TapeNotFoundRuntimeException(id);
        }
    }

    private static TapeTitle subtitleOf(String subtitle) {
        return subtitle != null ? new TapeTitle(subtitle) : null;
    }

    private static <T> T orExisting(T candidate, T existing) {
        return candidate != null ? candidate : existing;
    }
}
