package com.jaqqen.tapeshub.domain.tape;

/**
 * A tape in the gallery - a song, video, or anything else that ships on a sleeve.
 * Fields mirror the {@code Tape} interface in web-portal's tapes.ts; {@code year}
 * and {@code rating} are free-form strings there, so they stay strings here.
 * {@code subtitle} is optional and may be null.
 */
public record Tape(
        String id,
        String title,
        String subtitle,
        String year,
        String genre,
        String duration,
        String rating,
        String description,
        TapeColors colors,
        TapePattern pattern
) {
}
