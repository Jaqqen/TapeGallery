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

    @Column(nullable = false, length = 9)
    private String central;

    @Column(nullable = false, length = 9)
    private String secondary;

    @Column(nullable = false, length = 9)
    private String accent;

    @Column(nullable = false, length = 9)
    private String label;
}
