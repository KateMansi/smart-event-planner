package com.mansi.planner.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Allows at most MAX_REQUESTS API calls per client per minute. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    static final int MAX_REQUESTS = 20;
    static final long WINDOW_SECONDS = 60;
    static final int MAX_TRACKED_CLIENTS = 10_000;

    private record Window(long startEpoch, int count) { }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimitFilter(Clock clock) {
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        long now = clock.instant().getEpochSecond();
        if (windows.size() > MAX_TRACKED_CLIENTS) {
            windows.clear();
        }
        Window window = windows.merge(req.getRemoteAddr(), new Window(now, 1),
                (old, fresh) -> now - old.startEpoch() >= WINDOW_SECONDS
                        ? fresh
                        : new Window(old.startEpoch(), old.count() + 1));

        if (window.count() > MAX_REQUESTS) {
            res.setStatus(429);
            res.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Too many requests. Please try again in a minute.\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}