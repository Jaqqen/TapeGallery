package com.jaqqen.tapeshub.tape.presentation.dto;

import com.jaqqen.tapeshub.tape.domain.TapeGenre;

import java.util.UUID;

public record TapeGenreDto(UUID id, String name, String description) {
    public static TapeGenreDto from(TapeGenre genre) {
        return new TapeGenreDto(genre.id(), genre.name(), genre.description());
    }
}
