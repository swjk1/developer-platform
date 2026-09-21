package com.leon.github;

import java.time.Duration;
import java.util.function.DoubleSupplier;

/**
 * Exponential backoff with jitter (section 12).
 *
 * <p>Written as a pure function rather than wired through Spring Retry so the
 * schedule can be asserted in a unit test without any sleeping. Section 37
 * lists exactly this as something that should be unit-tested.
 *
 * <p>Jitter matters more than the exponent. Without it, every client that
 * failed at the same moment retries at the same moment, and a service that is
 * struggling gets a synchronised second wave — the thundering herd. The jitter
 * here is full-range multiplicative: the delay is scaled by a random factor in
 * [1 - jitter, 1 + jitter].
 */
public final class Backoff {

    private Backoff() {
    }

    /**
     * @param attempt 1-based attempt number that has just failed
     * @param base    delay after the first failure
     * @param max     ceiling, so a long outage does not produce absurd waits
     * @param jitter  fraction in [0, 1]; 0.2 means plus or minus twenty percent
     * @param random  supplier of values in [0, 1)
     */
    public static Duration delayAfter(int attempt, Duration base, Duration max, double jitter, DoubleSupplier random) {
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt is 1-based, got " + attempt);
        }
        if (jitter < 0 || jitter > 1) {
            throw new IllegalArgumentException("jitter must be within [0, 1], got " + jitter);
        }

        // Shift rather than pow, and clamp the exponent: 1L << 63 overflows and
        // a long outage should not be the thing that discovers that.
        int exponent = Math.min(attempt - 1, 20);
        long scaled = base.toMillis() << exponent;
        long capped = Math.min(scaled, max.toMillis());

        double factor = 1.0 + ((random.getAsDouble() * 2.0) - 1.0) * jitter;
        long withJitter = Math.round(capped * factor);

        // A zero or negative delay would turn a retry loop into a spin loop.
        return Duration.ofMillis(Math.max(1L, withJitter));
    }
}
