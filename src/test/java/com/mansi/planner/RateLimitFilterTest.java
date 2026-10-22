package com.mansi.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mansi.planner.web.RateLimitFilter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

    private final Clock fixed = Clock.fixed(Instant.parse("2026-11-01T00:00:00Z"), ZoneOffset.UTC);

    private int call(RateLimitFilter filter, String ip, String uri) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
        req.setRemoteAddr(ip);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        return res.getStatus();
    }

    @Test
    void blocksClientAfterLimitIsReached() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(fixed);
        for (int i = 0; i < 20; i++) {
            assertEquals(200, call(filter, "10.0.0.1", "/api/plan"));
        }
        assertEquals(429, call(filter, "10.0.0.1", "/api/plan"));
    }

    @Test
    void otherClientsAreNotAffected() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(fixed);
        for (int i = 0; i < 25; i++) {
            call(filter, "10.0.0.1", "/api/plan");
        }
        assertEquals(200, call(filter, "10.0.0.2", "/api/plan"));
    }
}