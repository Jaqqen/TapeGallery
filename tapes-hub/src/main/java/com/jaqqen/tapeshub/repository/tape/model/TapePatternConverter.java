package com.jaqqen.tapeshub.repository.tape.model;

import com.jaqqen.tapeshub.domain.tape.TapePattern;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores a {@link TapePattern} as its kebab-case wire value, so the database holds
 * the same spelling as the JSON API and the web-portal's tapes.ts - one canonical
 * name for a pattern rather than {@code RETRO_BLOCKS} in one place and
 * {@code retro-blocks} in another.
 */
@Converter(autoApply = true)
public class TapePatternConverter implements AttributeConverter<TapePattern, String> {

    @Override
    public String convertToDatabaseColumn(TapePattern pattern) {
        return pattern == null ? null : pattern.getValue();
    }

    @Override
    public TapePattern convertToEntityAttribute(String value) {
        return value == null ? null : TapePattern.fromValue(value);
    }
}
