package com.jaqqen.tapeshub.domain.tape;

import java.util.UUID;

/**
 * A tape in the gallery - a song, video, or anything else that ships on a sleeve.
 * Fields mirror the {@code Tape} interface in web-portal's tapes.ts; {@code year}
 * and {@code rating} are free-form strings there, so they stay strings here.
 * {@code subtitle} is optional and may be null.
 *
 * <p>{@code id} is the identity: it is minted once, never changes, and is what the
 * API's URLs carry.
 */
public record Tape(
        UUID id,
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
