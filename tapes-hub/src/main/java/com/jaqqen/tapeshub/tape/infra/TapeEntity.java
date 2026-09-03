package com.jaqqen.tapeshub.tape.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * The {@code tape} row. A persistence detail - {@link com.jaqqen.tapeshub.tape.domain.Tape} is the model.
 *
 * <p>The genre is a bare {@code genre_id}, not a {@code @ManyToOne}: mapping an association across
 * an aggregate boundary would let a tape load, and eventually mutate, a genre. The foreign key in
 * V1__create_tapes.sql still enforces that the id points at a real row.
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

    private String subtitle;

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
