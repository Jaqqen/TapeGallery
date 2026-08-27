package com.jaqqen.tapeshub.tape.infra;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tape")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(builderClassName = "Builder")
public class TapeEntity {
    @Id
    private UUID id;
    private String title;
    private String subtitle;
    private LocalDate releaseDate;
    @ManyToOne
    private TapeGenreEntity genre;
    private int duration;
    @ManyToOne
    private TapeColorsEntity colors;
    private String pattern;
}
