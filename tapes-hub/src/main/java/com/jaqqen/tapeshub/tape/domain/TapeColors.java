package com.jaqqen.tapeshub.tape.domain;

import java.util.UUID;

/**
 * The four hex colours a tape's sleeve is rendered with.
 * Mirrors the {@code colors} object of the {@code Tape} interface in web-portal.
 */
public record TapeColors(UUID id, String primary, String secondary, String accent, String label) {
}
