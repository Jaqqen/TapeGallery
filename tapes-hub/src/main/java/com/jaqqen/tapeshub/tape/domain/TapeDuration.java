package com.jaqqen.tapeshub.tape.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

/** How long a tape runs, in milliseconds. */
@ValueObject
public record TapeDuration(int milliseconds) {

    public TapeDuration {
        Assert.isTrue(milliseconds > 0, "duration must be greater than zero");
    }
}
