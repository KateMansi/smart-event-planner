package com.mansi.planner.service;

import com.mansi.planner.model.EventRequest;
import com.mansi.planner.model.PlanResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Small in-memory cache so identical requests do not call Gemini again. */
@Component
public class PlanCache {

    static final int MAX_ENTRIES = 500;
    static final Duration TTL = Duration.ofHours(1);

    private record Entry(PlanResponse plan, Instant expiresAt) { }

    private final Map<EventRequest, Entry> store = new ConcurrentHashMap<>();
    private final Clock clock;

    public PlanCache(Clock clock) {
        this.clock = clock;
    }

    public Optional<PlanResponse> get(EventRequest key) {
        Entry entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt().isBefore(Instant.now(clock))) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.plan());
    }

    public void put(EventRequest key, PlanResponse plan) {
        if (store.size() >= MAX_ENTRIES) {
            store.clear();
        }
        store.put(key, new Entry(plan, Instant.now(clock).plus(TTL)));
    }
}