package com.jaqqen.tapeshub.tape.infra;

import com.jaqqen.tapeshub.tape.domain.*;
import org.springframework.stereotype.Service;

@Service
public class TapeMapper {
    TapeEntity toEntity(Tape tape) {
        return new TapeEntity.Builder()
            .id(tape.getId().id())
            .title(tape.getTitle().title())
            .subtitle(tape.getSubtitle() != null ? tape.getSubtitle().title() : null)
            .releaseDate(tape.getReleaseDate())
            .genre(toTapeGenreEntity(tape.getGenre()))
            .duration(tape.getDuration().milliseconds())
            .colors(toTapeColorsEntity(tape.getColors()))
            .pattern(tape.getPattern().getValue())
            .build();
    }
    Tape toDomain(TapeEntity entity) {
        return Tape.builder()
            .id(new TapeId(entity.getId()))
            .title(new TapeTitle(entity.getTitle()))
            .subtitle(entity.getSubtitle() != null ? TapeTitle.asSubtitle(entity.getSubtitle()) : null)
            .releaseDate(entity.getReleaseDate())
            .genre(toTapeGenre(entity.getGenre()))
            .duration(new TapeDuration(entity.getDuration()))
            .colors(toTapeColors(entity.getColors()))
            .pattern(TapePattern.fromValue(entity.getPattern()))
            .build();
    }

    static TapeGenreEntity toTapeGenreEntity(TapeGenre genre) {
        return new TapeGenreEntity(genre.id(), genre.name(), genre.description());
    }

    static TapeGenre toTapeGenre(TapeGenreEntity entity) {
        return new TapeGenre(entity.getId(), entity.getName(), entity.getDescription());
    }
    static TapeColorsEntity toTapeColorsEntity(TapeColors colors) {
        return new TapeColorsEntity(colors.id(), colors.primary(), colors.secondary(), colors.accent(), colors.label());
    }

    static TapeColors toTapeColors(TapeColorsEntity entity) {
        return new TapeColors(entity.getId(), entity.getPrimary(), entity.getSecondary(), entity.getAccent(), entity.getLabel());
    }
}
