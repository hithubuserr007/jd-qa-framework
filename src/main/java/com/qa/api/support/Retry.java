package com.qa.api.support;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Bounded polling for eventually-consistent systems.
 *
 * The public Petstore sits behind a load balancer with several nodes that do not share state
 * instantly, so a GET straight after a POST/DELETE can intermittently return a stale result.
 * Rather than adding blind sleeps or retrying the whole test, we poll the *specific* call
 * until it reaches the expected state, with a hard upper bound, and return the last result
 * so the assertion that follows still reports the real outcome.
 */
public final class Retry {

    private Retry() {
    }

    public static <T> T until(Supplier<T> action, Predicate<T> condition, int maxAttempts, Duration delay) {
        T result = action.get();
        for (int attempt = 1; attempt < maxAttempts && !condition.test(result); attempt++) {
            sleep(delay);
            result = action.get();
        }
        return result;
    }

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling", e);
        }
    }
}
