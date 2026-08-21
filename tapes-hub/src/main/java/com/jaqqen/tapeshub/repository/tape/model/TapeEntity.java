package com.jaqqen.tapeshub.repository.tape.model;

import com.jaqqen.tapeshub.domain.tape.Tape;
import com.jaqqen.tapeshub.domain.tape.TapePattern;
import com.jaqqen.tapeshub.repository.tape.TapeRepository;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

/**
 * The tapes table. Package-private on purpose: nothing outside this package sees a
 * JPA type, which is what keeps the {@link TapeRepository} seam swappable.
 *
 * <p>{@code id} is a surrogate key that never changes. The domain's {@code Tape.id}
 * is the slug, which is derived from the title and therefore moves when a tape is
 * retitled - not something a cross-store reference can rely on.
 */
@Entity
@Table(name = "tapes")
public class TapeEntity {

    @Id
    @Getter
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    @Column(name = "year", nullable = false)
    private String year;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private String rating;

    @Column(nullable = false)
    private String description;

    @Embedded
    private TapeColorsEmbeddable colors;

    @Column(nullable = false)
    private TapePattern pattern;

    protected TapeEntity() {
    }

    public static TapeEntity fromDomain(UUID id, Tape tape) {
        TapeEntity entity = new TapeEntity();
        entity.id = id;
        entity.apply(tape);
        return entity;
    }

    /** Overwrites every mutable field from the given tape, keeping the surrogate id. */
    public void apply(Tape tape) {
        this.slug = tape.id();
        this.title = tape.title();
        this.subtitle = tape.subtitle();
        this.year = tape.year();
        this.genre = tape.genre();
        this.duration = tape.duration();
        this.rating = tape.rating();
        this.description = tape.description();
        this.colors = TapeColorsEmbeddable.fromDomain(tape.colors());
        this.pattern = tape.pattern();
    }

    public Tape toDomain() {
        return new Tape(slug, title, subtitle, year, genre, duration, rating, description,
                colors.toDomain(), pattern);
    }
}
