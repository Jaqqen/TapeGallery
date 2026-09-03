package com.jaqqen.tapeshub.tape.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * The {@code tape} row. A persistence detail - {@link com.jaqqen.tapeshub.tape.domain.Tape} is the model.
 *
 * <p>
 *     Other domain object (genre) is identified by {@link #genreId}. No {@code @ManyToOne}:
 *     foreign key exists in actual database table.
 * </p>
 */
@Entity
@Table(name = "tape")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class TapeEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private @Nullable String subtitle;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Column(name = "genre_id", nullable = false)
    private UUID genreId;

    @Column(nullable = false)
    private int duration;

    @Embedded
    private TapeColorsEmbeddable colors;

    @Column(nullable = false, length = 32)
    private String pattern;
}
