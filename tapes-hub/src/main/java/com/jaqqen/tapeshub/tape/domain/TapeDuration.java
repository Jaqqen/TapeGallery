package com.jaqqen.tapeshub.tape.domain;

import org.springframework.util.Assert;

public record TapeDuration(int milliseconds) {
    public TapeDuration {
        Assert.isTrue(milliseconds > 0, "duration must be greater than zero");
    }


}
