package com.jaqqen.tapeshub.tape.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Sleeve pattern of a tape. The wire format is kebab-case to match the
 * {@code pattern} union in web-portal's tapes.ts, which the shelf component
 * switches on. Adding a value here means adding it there too.
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
