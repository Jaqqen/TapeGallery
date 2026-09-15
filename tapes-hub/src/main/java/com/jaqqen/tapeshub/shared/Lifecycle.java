package com.jaqqen.tapeshub.shared;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * A container to store alterations such as creation date, modification date or deletion date (eventually, even the user
 * who triggered it);
 *
 * <p>Every stamp is truncated to microseconds. Postgres {@code timestamptz} stores no more than that,
 * so an untruncated {@link Instant} would come back from the database unequal to the one that went in.
 */
@ValueObject
public record Lifecycle(Instant createdAt, Instant modifiedAt, @Nullable Instant deletedAt) {

    public Lifecycle {
        Assert.isTrue(!modifiedAt.isBefore(createdAt),
            () -> "modifiedAt %s cannot precede createdAt %s".formatted(modifiedAt, createdAt));
        Assert.isTrue(deletedAt == null || !deletedAt.isBefore(createdAt),
            () -> "deletedAt %s cannot precede createdAt %s".formatted(deletedAt, createdAt));
    }

    /** The stamp a brand-new aggregate starts with: created and modified at the same instant. */
    public static Lifecycle start() {
        final Instant now = nowInMicros();
        return new Lifecycle(now, now, null);
    }

    /** Records that the aggregate changed. {@code createdAt} is history and never moves. */
    public Lifecycle modify() {
        return new Lifecycle(createdAt, nowInMicros(), deletedAt);
    }

    /**
     * Marks the aggregate deleted. Deleting is itself a change, so it moves {@code modifiedAt} too.
     * Deleting twice keeps the first {@code deletedAt}: when it went is the fact worth holding on to.
     */
    public Lifecycle delete() {
        final Instant now = nowInMicros();
        return new Lifecycle(createdAt, now, now);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    private static Instant nowInMicros() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }
}
