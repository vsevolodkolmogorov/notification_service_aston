package ru.astondevs.notificationserviceaston.common;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {

    private enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final long openTimeoutMs;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile State state = State.CLOSED;
    private volatile Instant lastFailureTime;

    public CircuitBreaker(int failureThreshold, long openTimeoutMs) {
        this.failureThreshold = failureThreshold;
        this.openTimeoutMs = openTimeoutMs;
    }

    public synchronized boolean allowRequest() {
        if (state == State.OPEN) {
            if (Instant.now().isAfter(lastFailureTime.plusMillis(openTimeoutMs))) {
                state = State.HALF_OPEN;
                return true;
            }
            return false;
        }
        return true;
    }

    public synchronized void recordSuccess() {
        if (state != State.CLOSED) {
            System.out.println("Circuit closed after successful attempt");
        }
        state = State.CLOSED;
        failureCount.set(0);
    }

    public synchronized void recordFailure() {
        int failures = failureCount.incrementAndGet();
        if (failures >= failureThreshold) {
            state = State.OPEN;
            lastFailureTime = Instant.now();
            System.out.println("Circuit opened due to repeated failures!");
        }
    }

    public String getState() {
        return state.name();
    }
}
