package com.jaqqen.tapeshub.tape.infra;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Table(name = "tape_colors")
@AllArgsConstructor
@NoArgsConstructor
public class TapeColorsEntity {
    @Id
    @Setter(AccessLevel.NONE)
    private UUID id;
    @NotEmpty
    private String primary;
    @NotEmpty
    private String secondary;
    @NotEmpty
    private String accent;
    @NotEmpty
    private String label;
}


