/**
 * Application-wide wiring: Flyway, HTTP security, and the error handling that is not specific to any
 * one module.
 *
 * <p>These used to live inside {@code tape.presentation}, which made a feature module the owner of
 * concerns that were never its own. Nothing here imports a module's internals, so it is a
 * dependency-free module in its own right.
 */
@ApplicationModule(displayName = "Configuration")
package com.jaqqen.tapeshub.config;

import org.springframework.modulith.ApplicationModule;
