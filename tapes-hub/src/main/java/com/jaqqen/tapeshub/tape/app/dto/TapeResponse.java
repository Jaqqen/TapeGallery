package com.jaqqen.tapeshub.tape.app.dto;

import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapePattern;

import java.time.LocalDate;
import java.util.UUID;

public record TapeResponse(
        UUID id,
        String title,
        String subtitle,
        LocalDate releaseDate,
        TapeGenreDto genre,
        int duration,
        TapeColorsDto colors,
        TapePattern pattern
) {
    public static TapeResponse from(Tape tape) {
        return new TapeResponse(
                tape.getId().id(),
                tape.getTitle().title(),
                tape.getSubtitle() != null ? tape.getSubtitle().title() : null,
                tape.getReleaseDate(),
                TapeGenreDto.from(tape.getGenre()),
                tape.getDuration().milliseconds(),
                TapeColorsDto.from(tape.getColors()),
                tape.getPattern()
        );
    }
}
