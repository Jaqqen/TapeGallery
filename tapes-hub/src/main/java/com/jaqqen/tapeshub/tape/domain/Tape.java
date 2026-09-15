package com.jaqqen.tapeshub.tape.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.shared.Lifecycle;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/**
 * A tape - audio or video - and the aggregate root of this module.
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
    private Lifecycle lifecycle;

    private Tape(TapeId id, TapeTitle title, @Nullable TapeTitle subtitle, LocalDate releaseDate, GenreId genre,
                 TapeDuration duration, Colors colors, TapePattern pattern, Lifecycle lifecycle) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.duration = duration;
        this.colors = colors;
        this.pattern = pattern;
        this.lifecycle = lifecycle;
    }

    public static Tape create(TapeTitle title, @Nullable TapeTitle subtitle, LocalDate releaseDate, GenreId genre,
                              TapeDuration duration, Colors colors, TapePattern pattern) {
        return new Tape(TapeId.newId(), title, subtitle, releaseDate, genre, duration, colors, pattern,
            Lifecycle.start());
    }

    public static Tape existing(TapeId id, TapeTitle title, @Nullable TapeTitle subtitle, LocalDate releaseDate,
                                GenreId genre, TapeDuration duration, Colors colors, TapePattern pattern,
                                Lifecycle lifecycle) {
        return new Tape(id, title, subtitle, releaseDate, genre, duration, colors, pattern, lifecycle);
    }

    public Tape rename(TapeTitle title) {
        this.title = title;
        return modify();
    }

    public Tape resubtitle(@Nullable TapeTitle subtitle) {
        if (subtitle != null) {
            this.subtitle = subtitle;
            return modify();
        }
        // Nothing was replaced, so nothing was modified.
        return this;
    }

    public Tape releasedOn(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
        return modify();
    }

    public Tape reclassify(GenreId genre) {
        this.genre = genre;
        return modify();
    }

    public Tape runsFor(TapeDuration duration) {
        this.duration = duration;
        return modify();
    }

    public Tape recolour(Colors colors) {
        this.colors = colors;
        return modify();
    }

    public Tape restyle(TapePattern pattern) {
        this.pattern = pattern;
        return modify();
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

    public Tape softDelete() {
        this.lifecycle = lifecycle.delete();
        return this;
    }

    public boolean isDeleted() {
        return lifecycle.isDeleted();
    }

    private Tape modify() {
        this.lifecycle = lifecycle.modify();
        return this;
    }
}
