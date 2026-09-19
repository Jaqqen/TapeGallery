package com.jaqqen.tapeshub.tape.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

/** A tape's title, or its optional subtitle - the same rules apply to both. */
@ValueObject
public record TapeTitle(String value) {

    private static final int MAX_LENGTH = 255;

    public TapeTitle {
        Assert.hasText(value, "title must not be blank");
        Assert.isTrue(value.length() <= MAX_LENGTH,
            () -> "title must be at most %d characters".formatted(MAX_LENGTH));
    }

    public static @Nullable TapeTitle ofNullable(@Nullable String value) {
        return value == null || value.isBlank() ? null : new TapeTitle(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
