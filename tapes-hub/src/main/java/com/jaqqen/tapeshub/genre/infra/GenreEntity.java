package com.jaqqen.tapeshub.genre.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** The {@code genre} row. A persistence detail - {@link com.jaqqen.tapeshub.genre.domain.Genre} is the model. */
@Entity
@Table(name = "genre")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class GenreEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String name;

    private String description;
}
