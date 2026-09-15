package com.jaqqen.tapeshub.shared;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class LifecycleTest {

    private static final Instant NOON = Instant.parse("2026-09-10T12:00:00Z");

    @Test
    void validateFreshLifecycleCreationBehavior() {
        final Lifecycle lifecycle = Lifecycle.start();

        assertThat(lifecycle.createdAt()).isEqualTo(lifecycle.modifiedAt());
        assertThat(lifecycle.deletedAt()).isNull();
        assertThat(lifecycle.isDeleted()).isFalse();
    }

    @Test
    void stampsAreTruncatedToMicroseconds() {
        // Postgres timestamptz stores microseconds
        final Lifecycle lifecycle = Lifecycle.start();

        assertThat(lifecycle.createdAt()).isEqualTo(lifecycle.createdAt().truncatedTo(ChronoUnit.MICROS));
    }

    @Test
    void testModification() {
        final Lifecycle created = new Lifecycle(NOON, NOON, null);
        final Lifecycle touched = created.modify();

        assertThat(created.modifiedAt()).isEqualTo(NOON);
        assertThat(touched).isNotSameAs(created);
        assertThat(touched.createdAt()).isEqualTo(NOON);
        assertThat(touched.modifiedAt()).isAfter(NOON);
        assertThat(touched.deletedAt()).isNull();
    }

    @Test
    void testDelete() {
        final Lifecycle created = new Lifecycle(NOON, NOON, null);
        final Lifecycle deleted = created.delete();

        assertThat(created.modifiedAt()).isEqualTo(NOON);
        assertThat(created.deletedAt()).isNull();
        assertThat(deleted).isNotSameAs(created);
        assertThat(deleted.deletedAt()).isNotNull();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.modifiedAt()).isEqualTo(deleted.deletedAt());
        assertThat(deleted.createdAt()).isEqualTo(NOON);
    }

    @Test
    void deletingTwiceKeepsTheFirstDeletedAt() {
        Lifecycle deleted = new Lifecycle(NOON, NOON, null).delete();

        Lifecycle again = deleted.delete();

        // When it went is the fact worth holding on to; a second delete is just another touch.
        assertThat(again.deletedAt()).isEqualTo(deleted.deletedAt());
        assertThat(again.modifiedAt()).isAfterOrEqualTo(deleted.modifiedAt());
    }

    @Test
    void modifiedAtCannotPrecedeCreatedAt() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Lifecycle(NOON, NOON.minusSeconds(1), null))
            .withMessageContaining("cannot precede createdAt");
    }

    @Test
    void deletedAtCannotPrecedeCreatedAt() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Lifecycle(NOON, NOON, NOON.minusSeconds(1)))
            .withMessageContaining("cannot precede createdAt");
    }

    @Test
    void createdAndModifiedMayBeTheSameInstant() {
        assertThat(new Lifecycle(NOON, NOON, NOON).isDeleted()).isTrue();
    }
}
