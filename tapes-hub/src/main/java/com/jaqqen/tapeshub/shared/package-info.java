/**
 * The shared kernel: the handful of concepts too small and too cross-cutting to belong to any one
 * feature module.
 *
 * <p>{@code tape} and {@code genre} each mint their own identity type, {@code TapeId} and
 * {@code GenreId}, but "an identifier must actually be present" is the same rule in both places -
 * genuinely owned by neither module. Rather than duplicate it, or force one module to depend on the
 * other's internals to reach it, it lives here instead. Keep this package deliberately small: it is
 * a shared kernel, not a place to put things that have not found a module yet.
 */
@ApplicationModule(displayName = "Shared")
package com.jaqqen.tapeshub.shared;

import org.springframework.modulith.ApplicationModule;
