package com.jaqqen.tapeshub.controller.dto.tape;

import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.domain.tape.TapePattern;

public record TapeResponse(
        String id,
        String title,
        String subtitle,
        String year,
        String genre,
        String duration,
        String rating,
        String description,
        TapeColorsDto colors,
        TapePattern pattern
) {
    public static TapeResponse from(Tape tape) {
        return new TapeResponse(
                tape.id(),
                tape.title(),
                tape.subtitle(),
                tape.year(),
                tape.genre(),
                tape.duration(),
                tape.rating(),
                tape.description(),
                TapeColorsDto.from(tape.colors()),
                tape.pattern()
        );
    }
}
