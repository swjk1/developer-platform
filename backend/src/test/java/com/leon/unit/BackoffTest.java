package com.leon.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.leon.github.Backoff;
import java.time.Duration;
import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.Test;

/**
 * Section 37 lists retry timing as something that should be unit-tested.
 * Because {@link Backoff} takes its randomness as a parameter, the whole
 * schedule is assertable without sleeping or flaking.
 */
class BackoffTest {

    private static final Duration BASE = Duration.ofMillis(500);
    private static final Duration MAX = Duration.ofSeconds(30);

    /** Pin the jitter to the midpoint so the exponent alone is under test. */
    private static final DoubleSupplier NO_JITTER = () -> 0.5;

    @Test
    void doublesTheDelayOnEachAttempt() {
        // The schedule spec section 12 asks for: 500 ms, 1 s, 2 s.
        assertThat(Backoff.delayAfter(1, BASE, MAX, 0.0, NO_JITTER)).isEqualTo(Duration.ofMillis(500));
        assertThat(Backoff.delayAfter(2, BASE, MAX, 0.0, NO_JITTER)).isEqualTo(Duration.ofSeconds(1));
        assertThat(Backoff.delayAfter(3, BASE, MAX, 0.0, NO_JITTER)).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void neverExceedsTheCeiling() {
        assertThat(Backoff.delayAfter(20, BASE, MAX, 0.0, NO_JITTER)).isEqualTo(MAX);
    }

    @Test
    void doesNotOverflowOnAnAbsurdAttemptNumber() {
        // A long outage must not be the thing that discovers a shift overflow.
        assertThat(Backoff.delayAfter(Integer.MAX_VALUE, BASE, MAX, 0.0, NO_JITTER))
                .isEqualTo(MAX)
                .isPositive();
    }

    @Test
    void jitterStaysWithinTheRequestedBand() {
        // At attempt 2 the un-jittered delay is 1000 ms; +/-20% is [800, 1200].
        Duration low = Backoff.delayAfter(2, BASE, MAX, 0.2, () -> 0.0);
        Duration high = Backoff.delayAfter(2, BASE, MAX, 0.2, () -> 1.0);

        assertThat(low).isEqualTo(Duration.ofMillis(800));
        assertThat(high).isEqualTo(Duration.ofMillis(1200));
    }

    @Test
    void jitterSpreadsRetriesApart() {
        // The point of jitter: two clients that failed together must not
        // retry together.
        Duration first = Backoff.delayAfter(3, BASE, MAX, 0.5, () -> 0.1);
        Duration second = Backoff.delayAfter(3, BASE, MAX, 0.5, () -> 0.9);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void neverReturnsZero() {
        // A zero delay would turn the retry loop into a spin loop.
        Duration tiny = Backoff.delayAfter(1, Duration.ofMillis(1), MAX, 1.0, () -> 0.0);

        assertThat(tiny).isPositive();
    }

    @Test
    void rejectsNonsenseInput() {
        assertThatThrownBy(() -> Backoff.delayAfter(0, BASE, MAX, 0.2, NO_JITTER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Backoff.delayAfter(1, BASE, MAX, 1.5, NO_JITTER))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
