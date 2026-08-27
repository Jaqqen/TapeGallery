package com.jaqqen.tapeshub.tape.domain;

import org.springframework.util.Assert;

public record TapeTitle(String title, boolean subtitle) {
    public TapeTitle {
        Assert.notNull(title, "Title must not be null");
        Assert.isTrue(!title.isEmpty(), "Title must not be empty");
    }

    public TapeTitle(String title) {
        this(title, false);
    }

    public static TapeTitle asSubtitle(String title) {
        return new TapeTitle(title, true);
    }
}
