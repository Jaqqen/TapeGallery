package com.jaqqen.tapeshub.tape.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A tape - audio or video - and the aggregate root of this module.
 *
 * <p>It holds a {@link GenreId}, not a genre. An audio or video always has a genre, and this
 * package being {@code @NullMarked} is where that rule lives: every field below is non-null unless
 * marked {@link Nullable}, and NullAway fails the build on any caller that ignores it. The
 * constructor checks again at runtime because {@link #existing} is fed by Hibernate reflection,
 * which the compiler cannot vouch for. That the id points at a genre which actually exists is
 * checked one layer out, by {@code TapeService}, since only the application ring may talk to
 * another module.
 *
 * <p>Built through {@link #create} - which mints the identity - or {@link #existing}, which rebuilds
 * one the database already holds. Changes go through the named operations below rather than
 * setters, so every transition is one the aggregate agreed to.
 */
@Getter
public class Tape implements AggregateRoot<Tape, TapeId> {

    private final TapeId id;
    private TapeTitle title;
    /** Optional: plenty of tapes have no subtitle. */
    private @Nullable TapeTitle subtitle;
    private LocalDate releaseDate;
    private GenreId genre;
    private TapeDuration duration;
    private Colors colors;
    private TapePattern pattern;

    private Tape(TapeId id, TapeTitle title, @Nullable TapeTitle subtitle, LocalDate releaseDate, GenreId genre,
                 TapeDuration duration, Colors colors, TapePattern pattern) {
        this.id = Objects.requireNonNull(id, "tape id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.subtitle = subtitle;
        this.releaseDate = Objects.requireNonNull(releaseDate, "release date must not be null");
        this.genre = Objects.requireNonNull(genre, "a tape must have a genre");
        this.duration = Objects.requireNonNull(duration, "duration must not be null");
        this.colors = Objects.requireNonNull(colors, "colors must not be null");
        this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
    }

    /** A brand-new tape. The identity is minted here, so it cannot be passed in. */
    public static Tape create(TapeTitle title, @Nullable TapeTitle subtitle, LocalDate releaseDate, GenreId genre,
                              TapeDuration duration, Colors colors, TapePattern pattern) {
        return new Tape(TapeId.newId(), title, subtitle, releaseDate, genre, duration, colors, pattern);
    }

    /** Rebuilds a tape that already exists, carrying its persisted identity. */
    public static Tape existing(TapeId id, TapeTitle title, @Nullable TapeTitle subtitle, LocalDate releaseDate,
                                GenreId genre, TapeDuration duration, Colors colors, TapePattern pattern) {
        return new Tape(id, title, subtitle, releaseDate, genre, duration, colors, pattern);
    }

    public Tape rename(TapeTitle title) {
        this.title = title;
        return this;
    }

    /** {@code null} removes the subtitle. */
    public Tape resubtitle(@Nullable TapeTitle subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public Tape releasedOn(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public Tape reclassify(GenreId genre) {
        this.genre = genre;
        return this;
    }

    public Tape runsFor(TapeDuration duration) {
        this.duration = duration;
        return this;
    }

    public Tape recolour(Colors colors) {
        this.colors = colors;
        return this;
    }

    public Tape restyle(TapePattern pattern) {
        this.pattern = pattern;
        return this;
    }

    /** Every mutable field at once, for a PUT. The identity is untouched - that is what PUT means here. */
    public Tape replaceWith(TapeTitle title, @Nullable TapeTitle subtitle, LocalDate releaseDate, GenreId genre,
                            TapeDuration duration, Colors colors, TapePattern pattern) {
        return rename(title)
            .resubtitle(subtitle)
            .releasedOn(releaseDate)
            .reclassify(genre)
            .runsFor(duration)
            .recolour(colors)
            .restyle(pattern);
    }
}
