package com.jaqqen.tapeshub.support;

import com.jaqqen.tapeshub.genre.app.dto.GenreRequest;
import com.jaqqen.tapeshub.tape.app.dto.TapeColorsDto;
import com.jaqqen.tapeshub.tape.app.dto.TapeRequest;
import com.jaqqen.tapeshub.tape.domain.TapePattern;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Builders for the real wire DTOs, so tests never re-declare shapes {@code TapeController} and
 * {@code GenreController} already own.
 */
public final class TapeRequests {

    public static final TapeColorsDto RETROWAVE_COLORS =
        new TapeColorsDto("#ff006e", "#8338ec", "#3a86ff", "#fb5607");

    private TapeRequests() {
    }

    public static GenreRequest genre(String name) {
        return new GenreRequest(name, "%s tapes.".formatted(name));
    }

    public static TapeRequest tape(UUID genreId, String title) {
        return tape(genreId, title, TapePattern.RETRO_BLOCKS);
    }

    public static TapeRequest tape(UUID genreId, String title, TapePattern pattern) {
        return new TapeRequest(
            title,
            "A Retrowave Journey",
            LocalDate.of(1987, 6, 12),
            genreId,
            5_400_000,
            RETROWAVE_COLORS,
            pattern);
    }
}
