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
 * <p>Built through {@link #create} - which auto generates the id - or {@link #existing}, which rebuilds
 * one the database already holds. Changes go through the named operations to enforce business rules set
 * by Tape.
 */
@Getter
public class Tape implements AggregateRoot<Tape, TapeId> {

    private final TapeId id;
    private TapeTitle title;
    /** Optional: Subtitles are not mandatory for a Tape */
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

    public static Tape create(TapeTitle title, @Nullable TapeTitle subtitle, LocalDate releaseDate, GenreId genre,
                              TapeDuration duration, Colors colors, TapePattern pattern) {
        return new Tape(TapeId.newId(), title, subtitle, releaseDate, genre, duration, colors, pattern);
    }

    public static Tape existing(TapeId id, TapeTitle title, @Nullable TapeTitle subtitle, LocalDate releaseDate,
                                GenreId genre, TapeDuration duration, Colors colors, TapePattern pattern) {
        return new Tape(id, title, subtitle, releaseDate, genre, duration, colors, pattern);
    }

    public Tape rename(TapeTitle title) {
        this.title = title;
        return this;
    }

    public Tape resubtitle(@Nullable TapeTitle subtitle) {
        if (subtitle != null) {
            this.subtitle = subtitle;
        }
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
