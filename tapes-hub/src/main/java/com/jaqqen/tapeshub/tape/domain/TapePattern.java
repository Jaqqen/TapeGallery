package com.jaqqen.tapeshub.tape.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Pattern value is exactly as it's in {@code web-portal}
 *
 * <p>
 *     {@link JsonValue} and {@link JsonCreator} prevents constant names to be sent to frontend.
 * </p>
 * <p>
 *     Example:<br>
 *     Without them Jackson would emit the constant name, so {@code RETRO_BLOCKS} would reach a
 *     frontend expecting {@code retro-blocks}.
 * </p>
 */
public enum TapePattern {
    STRIPES("stripes"),
    GRADIENT("gradient"),
    GEOMETRIC("geometric"),
    RETRO_BLOCKS("retro-blocks"),
    WAVES("waves"),
    DIAMONDS("diamonds");

    private final String value;

    TapePattern(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TapePattern fromValue(String value) {
        for (TapePattern pattern : values()) {
            if (pattern.value.equals(value)) {
                return pattern;
            }
        }
        throw new IllegalArgumentException("Unknown tape pattern: " + value);
    }
}
