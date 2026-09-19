package com.jaqqen.tapeshub.tape.app.dto;

import com.jaqqen.tapeshub.genre.GenreDetails;
import com.jaqqen.tapeshub.shared.Lifecycle;
import com.jaqqen.tapeshub.tape.domain.Tape;
import com.jaqqen.tapeshub.tape.domain.TapePattern;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * What every tape endpoint returns.
 *
 * <p>Requests name a genre by id; responses carry the whole {@link GenreDetails} so a client can
 * render a tape without a second round trip.
 */
public record TapeResponse(
    UUID id,
    String title,
    @Nullable String subtitle,
    LocalDate releaseDate,
    GenreDetails genre,
    int duration,
    TapeColorsDto colors,
    TapePattern pattern,
    Instant createdAt,
    Instant modifiedAt,
    @Nullable Instant deletedAt
) {

    public static TapeResponse from(Tape tape, GenreDetails genre) {
        Lifecycle lifecycle = tape.getLifecycle();
        return new TapeResponse(
            tape.getId().value(),
            tape.getTitle().value(),
            tape.getSubtitle() != null ? tape.getSubtitle().value() : null,
            tape.getReleaseDate(),
            genre,
            tape.getDuration().milliseconds(),
            TapeColorsDto.from(tape.getColors()),
            tape.getPattern(),
            lifecycle.createdAt(),
            lifecycle.modifiedAt(),
            lifecycle.deletedAt());
    }
}
