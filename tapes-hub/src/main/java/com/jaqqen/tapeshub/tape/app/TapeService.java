package com.jaqqen.tapeshub.tape.app;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.genre.Genres;
import com.jaqqen.tapeshub.tape.app.dto.PatchTapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeResponse;
import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapeDuration;
import com.jaqqen.tapeshub.tape.domain.TapeId;
import com.jaqqen.tapeshub.tape.domain.TapeNotFoundException;
import com.jaqqen.tapeshub.tape.domain.TapeRepository;
import com.jaqqen.tapeshub.tape.domain.TapeTitle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The tape module's only application service: it owns every use case the REST layer offers.
 *
 * <p>Its two collaborators are the tape repository and {@link Genres}, the narrow read-only view the
 * genre module publishes. Every write resolves the requested genre through that port first, which is
 * the single place enforcing "a tape cannot exist without a genre" against genres that actually
 * exist - the aggregate can only insist that the id is present, not that it resolves.
 */
@Service
@Transactional
public class TapeService {

    private final TapeRepository tapes;
    private final Genres genres;

    public TapeService(TapeRepository tapes, Genres genres) {
        this.tapes = tapes;
        this.genres = genres;
    }

    @Transactional(readOnly = true)
    public List<TapeResponse> list() {
        List<Tape> all = tapes.findAll();
        Set<GenreId> referenced = all.stream()
            .map(Tape::getGenre)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // One lookup for the whole page rather than one per tape.
        Map<GenreId, GenreDetails> byId = genres.findAllByIds(referenced);
        return all.stream().map(tape -> TapeResponse.from(tape, resolve(byId, tape.getGenre()))).toList();
    }

    @Transactional(readOnly = true)
    public TapeResponse get(UUID id) {
        Tape tape = load(new TapeId(id));
        return TapeResponse.from(tape, resolve(tape.getGenre()));
    }

    public TapeResponse create(TapeRequest request) {
        GenreDetails genre = resolve(new GenreId(request.genreId()));
        Tape tape = Tape.create(
            new TapeTitle(request.title()),
            TapeTitle.ofNullable(request.subtitle()),
            request.releaseDate(),
            new GenreId(genre.id()),
            new TapeDuration(request.duration()),
            request.colors().toDomain(),
            request.pattern());
        return TapeResponse.from(tapes.save(tape), genre);
    }

    public TapeResponse replace(UUID id, TapeRequest request) {
        GenreDetails genre = resolve(new GenreId(request.genreId()));
        Tape tape = load(new TapeId(id)).replaceWith(
            new TapeTitle(request.title()),
            TapeTitle.ofNullable(request.subtitle()),
            request.releaseDate(),
            new GenreId(genre.id()),
            new TapeDuration(request.duration()),
            request.colors().toDomain(),
            request.pattern());
        return TapeResponse.from(tapes.save(tape), genre);
    }

    /**
     * Applies only the fields the request actually carried. Each one maps to a named operation on
     * the aggregate, so the tape - not this service - decides what each change means.
     */
    public TapeResponse patch(UUID id, PatchTapeRequest request) {
        Tape tape = load(new TapeId(id));

        if (request.title() != null) {
            tape.rename(new TapeTitle(request.title()));
        }
        if (request.subtitle() != null) {
            tape.resubtitle(TapeTitle.ofNullable(request.subtitle()));
        }
        if (request.releaseDate() != null) {
            tape.releasedOn(request.releaseDate());
        }
        if (request.genreId() != null) {
            tape.reclassify(new GenreId(request.genreId()));
        }
        if (request.duration() != null) {
            tape.runsFor(new TapeDuration(request.duration()));
        }
        if (request.colors() != null) {
            tape.recolour(request.colors().toDomain());
        }
        if (request.pattern() != null) {
            tape.restyle(request.pattern());
        }

        // Resolved after the fact so a reclassification is validated on the way out too.
        GenreDetails genre = resolve(tape.getGenre());
        return TapeResponse.from(tapes.save(tape), genre);
    }

    public void delete(UUID id) {
        TapeId tapeId = new TapeId(id);
        if (!tapes.deleteById(tapeId)) {
            throw new TapeNotFoundException(tapeId);
        }
    }

    private Tape load(TapeId id) {
        return tapes.findById(id).orElseThrow(() -> new TapeNotFoundException(id));
    }

    private GenreDetails resolve(GenreId id) {
        return genres.findById(id).orElseThrow(() -> new UnknownGenreException(id));
    }

    /**
     * The batch equivalent of {@link #resolve(GenreId)}. A tape referencing a genre the lookup did
     * not return means the two modules disagree, which is the same failure {@code resolve} reports
     * one tape at a time - so it is reported the same way rather than surfacing as an NPE further
     * down.
     */
    private static GenreDetails resolve(Map<GenreId, GenreDetails> byId, GenreId id) {
        GenreDetails genre = byId.get(id);
        if (genre == null) {
            throw new UnknownGenreException(id);
        }
        return genre;
    }
}
