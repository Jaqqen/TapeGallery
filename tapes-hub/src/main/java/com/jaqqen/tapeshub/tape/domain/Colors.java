package com.jaqqen.tapeshub.tape.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.regex.Pattern;

@ValueObject
public record Colors(String primary, String secondary, String accent, String label) {

    /** Also used by the request DTO, so a bad payload is rejected as a 400 before it reaches here. */
    public static final String HEX_PATTERN = "^#(?:[0-9a-fA-F]{3}){1,2}$";

    private static final Pattern HEX = Pattern.compile(HEX_PATTERN);

    public Colors {
        requireHex(primary, "primary");
        requireHex(secondary, "secondary");
        requireHex(accent, "accent");
        requireHex(label, "label");
    }

    private static void requireHex(String value, String name) {
        Assert.isTrue(HEX.matcher(value).matches(),
            () -> "%s must be a hex colour such as #ff006e, but was '%s'".formatted(name, value));
    }
}
