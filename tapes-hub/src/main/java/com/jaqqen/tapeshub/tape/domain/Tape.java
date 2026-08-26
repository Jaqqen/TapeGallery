package com.jaqqen.tapeshub.tape.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder(builderClassName = "Builder")
@AllArgsConstructor
@Getter
public class Tape {
    private TapeId id;
    private TapeTitle title;
    private TapeTitle subtitle;
    private LocalDate releaseDate;
    private TapeGenre genre;
    private TapeDuration duration;
    private TapeColors colors;
    private TapePattern pattern;

    public Tape(TapeTitle title, TapeTitle subtitle, LocalDate releaseDate, TapeGenre genre, TapeDuration duration, TapeColors colors, TapePattern pattern) {
        this.id = new TapeId();
        this.title = title;
        this.subtitle = subtitle;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.duration = duration;
        this.colors = colors;
        this.pattern = pattern;
    }


}
