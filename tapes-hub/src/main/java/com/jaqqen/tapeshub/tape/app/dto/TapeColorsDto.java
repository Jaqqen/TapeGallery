package com.jaqqen.tapeshub.tape.app.dto;

import com.jaqqen.tapeshub.tape.domain.Colors;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TapeColorsDto(
    @NotNull @Pattern(regexp = Colors.HEX_PATTERN, message = "must be a hex colour such as #ff006e") String primary,
    @NotNull @Pattern(regexp = Colors.HEX_PATTERN, message = "must be a hex colour such as #ff006e") String secondary,
    @NotNull @Pattern(regexp = Colors.HEX_PATTERN, message = "must be a hex colour such as #ff006e") String accent,
    @NotNull @Pattern(regexp = Colors.HEX_PATTERN, message = "must be a hex colour such as #ff006e") String label
) {

    public Colors toDomain() {
        return new Colors(primary, secondary, accent, label);
    }

    public static TapeColorsDto from(Colors colors) {
        return new TapeColorsDto(colors.primary(), colors.secondary(), colors.accent(), colors.label());
    }
}
