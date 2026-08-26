package com.jaqqen.tapeshub.tape.infra;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "genre")
public class TapeGenreEntity {
    @Id
    @Setter(AccessLevel.NONE)
    private UUID id;
    @NotEmpty
    private String name;
    private String description;

}
