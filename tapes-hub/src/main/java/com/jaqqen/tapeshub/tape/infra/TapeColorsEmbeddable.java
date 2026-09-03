package com.jaqqen.tapeshub.tape.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class TapeColorsEmbeddable {

    /** {@code primary} is a reserved SQL keyword, so the name has to stay explicitly quoted. */
    @Column(name = "\"primary\"", nullable = false, length = 9)
    private String primary;

    @Column(nullable = false, length = 9)
    private String secondary;

    @Column(nullable = false, length = 9)
    private String accent;

    @Column(nullable = false, length = 9)
    private String label;
}
