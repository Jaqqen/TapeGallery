package com.jaqqen.tapeshub.service;

import com.jaqqen.tapeshub.controller.dto.tape.CreateTapeRequest;
import com.jaqqen.tapeshub.controller.dto.tape.PatchTapeRequest;
import com.jaqqen.tapeshub.controller.dto.tape.TapeColorsDto;
import com.jaqqen.tapeshub.controller.dto.tape.UpdateTapeRequest;
import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.exception.TapeNotFoundException;
import com.jaqqen.tapeshub.repository.tape.TapeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TapeService {

    private final TapeRepository repository;

    public TapeService(TapeRepository repository) {
        this.repository = repository;
    }

    public List<Tape> findAll() {
        return repository.findAll();
    }

    public Tape findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new TapeNotFoundException(id));
    }

    public Tape create(CreateTapeRequest request) {
        return repository.save(new Tape(
                UUID.randomUUID(),
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

    public Tape replace(UUID id, UpdateTapeRequest request) {
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

    public Tape patch(UUID id, PatchTapeRequest request) {
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

    public void delete(UUID id) {
        if (!repository.deleteById(id)) {
            throw new TapeNotFoundException(id);
        }
    }

    private static <T> T orExisting(T candidate, T existing) {
        return candidate != null ? candidate : existing;
    }
}
