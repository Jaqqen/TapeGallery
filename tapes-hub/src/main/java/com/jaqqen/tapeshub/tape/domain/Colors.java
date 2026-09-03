package com.jaqqen.tapeshub.tape.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.regex.Pattern;

/**
 * The four hex colours a tape's sleeve is rendered with, mirroring the {@code colors} object of
 * the {@code Tape} interface in web-portal.
 *
 * <p>Four strings with no identity and no lifecycle of their own: they live and die with the tape,
 * which is why they sit on the {@code tape} row rather than in a table of their own.
 */
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
        Assert.isTrue(value != null && HEX.matcher(value).matches(),
            () -> "%s must be a hex colour such as #ff006e, but was '%s'".formatted(name, value));
    }
}
