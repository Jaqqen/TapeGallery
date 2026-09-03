package com.jaqqen.tapeshub.tape.infra;

import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.tape.domain.Colors;
import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapeDuration;
import com.jaqqen.tapeshub.tape.domain.TapeId;
import com.jaqqen.tapeshub.tape.domain.TapePattern;
import com.jaqqen.tapeshub.tape.domain.TapeTitle;

/** Translates between the {@link Tape} aggregate and its row. Static: it holds no state. */
final class TapeMapper {

    private TapeMapper() {
    }

    static TapeEntity toEntity(Tape tape) {
        Colors colors = tape.getColors();
        return new TapeEntity(
            tape.getId().value(),
            tape.getTitle().value(),
            tape.getSubtitle() != null ? tape.getSubtitle().value() : null,
            tape.getReleaseDate(),
            tape.getGenre().value(),
            tape.getDuration().milliseconds(),
            new TapeColorsEmbeddable(colors.primary(), colors.secondary(), colors.accent(), colors.label()),
            tape.getPattern().getValue());
    }

    static Tape toDomain(TapeEntity entity) {
        TapeColorsEmbeddable colors = entity.getColors();
        return Tape.existing(
            new TapeId(entity.getId()),
            new TapeTitle(entity.getTitle()),
            TapeTitle.ofNullable(entity.getSubtitle()),
            entity.getReleaseDate(),
            new GenreId(entity.getGenreId()),
            new TapeDuration(entity.getDuration()),
            new Colors(colors.getPrimary(), colors.getSecondary(), colors.getAccent(), colors.getLabel()),
            TapePattern.fromValue(entity.getPattern()));
    }
}
