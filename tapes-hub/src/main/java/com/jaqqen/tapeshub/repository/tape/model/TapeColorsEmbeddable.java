package com.jaqqen.tapeshub.repository.tape.model;

import com.jaqqen.tapeshub.domain.tape.TapeColors;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Persistence-side counterpart of {@link TapeColors}, flattened into four columns
 * on the tapes table. Hibernate cannot map records, so the value object is
 * duplicated here rather than annotating the domain type.
 */
@Embeddable
public class TapeColorsEmbeddable {

    @Column(name = "color_primary", nullable = false, length = 9)
    private String primary;

    @Column(name = "color_secondary", nullable = false, length = 9)
    private String secondary;

    @Column(name = "color_accent", nullable = false, length = 9)
    private String accent;

    @Column(name = "color_label", nullable = false, length = 9)
    private String label;

    protected TapeColorsEmbeddable() {
    }

    static TapeColorsEmbeddable fromDomain(TapeColors colors) {
        TapeColorsEmbeddable embeddable = new TapeColorsEmbeddable();
        embeddable.primary = colors.primary();
        embeddable.secondary = colors.secondary();
        embeddable.accent = colors.accent();
        embeddable.label = colors.label();
        return embeddable;
    }

    TapeColors toDomain() {
        return new TapeColors(primary, secondary, accent, label);
    }
}
