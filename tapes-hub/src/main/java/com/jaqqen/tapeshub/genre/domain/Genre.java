package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;

import java.util.Objects;

/**
 * A genre a tape can belong to.
 *
 * <p>Built through {@link #create} - which mints the identity - or {@link #existing}, which
 * rebuilds one that the database already holds. There is no public constructor and no setter, so
 * a genre can only reach a state one of the named operations below produced.
 */
@Getter
public class Genre implements AggregateRoot<Genre, GenreId> {

    private final GenreId id;
    private GenreName name;
    private String description;

    private Genre(GenreId id, GenreName name, String description) {
        this.id = Objects.requireNonNull(id, "genre id must not be null");
        this.name = Objects.requireNonNull(name, "genre name must not be null");
        this.description = description;
    }

    /** A brand-new genre. The identity is minted here, so it cannot be passed in. */
    public static Genre create(GenreName name, String description) {
        return new Genre(GenreId.newId(), name, description);
    }

    /** Rebuilds a genre that already exists, carrying its persisted identity. */
    public static Genre existing(GenreId id, GenreName name, String description) {
        return new Genre(id, name, description);
    }

    public Genre rename(GenreName name) {
        this.name = Objects.requireNonNull(name, "genre name must not be null");
        return this;
    }

    /** The description is optional; {@code null} clears it. */
    public Genre describe(String description) {
        this.description = description;
        return this;
    }
}
