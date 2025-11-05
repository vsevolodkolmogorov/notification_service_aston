package ru.astondevs.notificationserviceaston.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CircuitBreakerRegistry {

    private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

    public CircuitBreaker getBreaker(String name) {
        return breakers.computeIfAbsent(name, s -> new CircuitBreaker(3, 10_000));
    }
}
