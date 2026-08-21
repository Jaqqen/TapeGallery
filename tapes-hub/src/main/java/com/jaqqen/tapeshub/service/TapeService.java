package com.jaqqen.tapeshub.service;

import com.jaqqen.tapeshub.controller.dto.tape.CreateTapeRequest;
import com.jaqqen.tapeshub.controller.dto.tape.PatchTapeRequest;
import com.jaqqen.tapeshub.controller.dto.tape.TapeColorsDto;
import com.jaqqen.tapeshub.controller.dto.tape.UpdateTapeRequest;
import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.exception.TapeAlreadyExistsException;
import com.jaqqen.tapeshub.exception.TapeNotFoundException;
import com.jaqqen.tapeshub.repository.tape.TapeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class TapeService {

    private static final Pattern NON_SLUG_CHARS = Pattern.compile("[^a-z0-9]+");

    private final TapeRepository repository;

    public TapeService(TapeRepository repository) {
        this.repository = repository;
    }

    public List<Tape> findAll() {
    return repository.findAll();
    }

    public Tape findById(String id) {
        return repository.findById(id).orElseThrow(() -> new TapeNotFoundException(id));
    }

    public Tape create(CreateTapeRequest request) {
        String id = hasText(request.id()) ? request.id() : slugify(request.title());
        if (repository.existsById(id)) {
            throw new TapeAlreadyExistsException(id);
        }
        return repository.save(new Tape(
                id,
                request.title(),
                request.subtitle(),
                request.year(),
                request.genre(),
                request.duration(),
                request.rating(),
                request.description(),
                request.colors().toDomain(),
                request.pattern()
        ));
    }

    public Tape replace(String id, UpdateTapeRequest request) {
        if (hasText(request.id()) && !Objects.equals(request.id(), id)) {
            throw new IllegalArgumentException(
                    "Body id '" + request.id() + "' does not match path id '" + id + "'");
        }
        if (!repository.existsById(id)) {
            throw new TapeNotFoundException(id);
        }
        return repository.save(new Tape(
                id,
                request.title(),
                request.subtitle(),
                request.year(),
                request.genre(),
                request.duration(),
                request.rating(),
                request.description(),
                request.colors().toDomain(),
                request.pattern()
        ));
    }

    public Tape patch(String id, PatchTapeRequest request) {
        Tape existing = findById(id);
        TapeColorsDto colors = request.colors();
        return repository.save(new Tape(
                id,
                orExisting(request.title(), existing.title()),
                orExisting(request.subtitle(), existing.subtitle()),
                orExisting(request.year(), existing.year()),
                orExisting(request.genre(), existing.genre()),
                orExisting(request.duration(), existing.duration()),
                orExisting(request.rating(), existing.rating()),
                orExisting(request.description(), existing.description()),
                colors != null ? colors.toDomain() : existing.colors(),
                orExisting(request.pattern(), existing.pattern())
        ));
    }

    public void delete(String id) {
        if (!repository.deleteById(id)) {
            throw new TapeNotFoundException(id);
        }
    }

    /**
     * Turns a title into a url-friendly id, e.g. "NEON NIGHTS" -> "neon-nights".
     */
    static String slugify(String title) {
        String slug = NON_SLUG_CHARS.matcher(title.toLowerCase(Locale.ROOT)).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        if (slug.isEmpty()) {
            throw new IllegalArgumentException("Cannot derive an id from title '" + title + "'");
        }
        return slug;
    }

    private static <T> T orExisting(T candidate, T existing) {
        return candidate != null ? candidate : existing;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
