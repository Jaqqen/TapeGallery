/**
 * Genres: the classification every tape carries.
 *
 * <p>A genre has a life of its own - it is created, renamed and described independently of any
 * tape - so it is an aggregate in its own right rather than a value object inside {@code Tape}.
 * The tape module is coupled to it by identity only.
 *
 * <p>This package <em>is</em> the module's published API: Spring Modulith exposes a module's base
 * package and hides everything below it. Only {@link com.jaqqen.tapeshub.genre.GenreId},
 * {@link com.jaqqen.tapeshub.genre.GenreDetails} and {@link com.jaqqen.tapeshub.genre.Genres} may
 * therefore be referenced from outside; {@code domain}, {@code app}, {@code infra} and
 * {@code presentation} are internal, and {@code ModularityTests} fails the build if that is broken.
 */
@ApplicationModule(displayName = "Genre")
@NullMarked
package com.jaqqen.tapeshub.genre;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
