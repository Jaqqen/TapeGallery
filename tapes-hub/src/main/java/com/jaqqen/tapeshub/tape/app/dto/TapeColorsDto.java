package com.jaqqen.tapeshub.tape.app.dto;

import com.jaqqen.tapeshub.tape.domain.TapeColors;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record TapeColorsDto(
        @NotBlank @Pattern(regexp = TapeColorsDto.HEX, message = "must be a hex colour such as #ff006e") String primary,
        @NotBlank @Pattern(regexp = TapeColorsDto.HEX, message = "must be a hex colour such as #ff006e") String secondary,
        @NotBlank @Pattern(regexp = TapeColorsDto.HEX, message = "must be a hex colour such as #ff006e") String accent,
        @NotBlank @Pattern(regexp = TapeColorsDto.HEX, message = "must be a hex colour such as #ff006e") String label
) {
    static final String HEX = "^#(?:[0-9a-fA-F]{3}){1,2}$";

    public TapeColors toDomain() {
        return new TapeColors(UUID.randomUUID(), primary, secondary, accent, label);
    }

    public static TapeColorsDto from(TapeColors colors) {
        return new TapeColorsDto(colors.primary(), colors.secondary(), colors.accent(), colors.label());
    }
}
