package com.jaqqen.tapeshub.tape.infra;

import jakarta.persistence.*;
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
    @Embedded
    private TapeColorsEntity colors;
    private String pattern;
}
