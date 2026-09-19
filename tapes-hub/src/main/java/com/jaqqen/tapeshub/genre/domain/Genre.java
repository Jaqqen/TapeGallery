package com.jaqqen.tapeshub.genre.domain;

import com.jaqqen.tapeshub.genre.GenreId;
import com.jaqqen.tapeshub.shared.Lifecycle;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jspecify.annotations.Nullable;

/**
 * A genre that describes a real life genre.
 */
@Getter
public class Genre implements AggregateRoot<Genre, GenreId> {

    private final GenreId id;
    private GenreName name;
    private @Nullable String description;
    private Lifecycle lifecycle;

    private Genre(GenreId id, GenreName name, @Nullable String description, Lifecycle lifecycle) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lifecycle = lifecycle;
    }

    /** A brand-new genre. The identity is minted here, so it cannot be passed in. */
    public static Genre create(GenreName name, @Nullable String description) {
        return new Genre(GenreId.newId(), name, description, Lifecycle.start());
    }

    /** Rebuilds a genre that already exists, carrying its persisted identity and lifecycle. */
    public static Genre existing(GenreId id, GenreName name, @Nullable String description,
                                 Lifecycle lifecycle) {
        return new Genre(id, name, description, lifecycle);
    }

    public Genre rename(GenreName name) {
        this.name = name;
        return modify();
    }

    public Genre describe(@Nullable String description) {
        this.description = description;
        return modify();
    }

    public Genre softDelete() {
        this.lifecycle = lifecycle.delete();
        return this;
    }

    public boolean isDeleted() {
        return lifecycle.isDeleted();
    }

    private Genre modify() {
        this.lifecycle = lifecycle.modify();
        return this;
    }
}
