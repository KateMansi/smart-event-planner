package com.mansi.planner.web;

import com.mansi.planner.model.EventRequest;
import com.mansi.planner.model.PlanResponse;
import com.mansi.planner.service.GeminiClient;
import com.mansi.planner.service.PlanCache;
import com.mansi.planner.service.PlannerService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PlanController {

    private static final String AI_FALLBACK =
            "AI summary is unavailable right now. Your rule-based plan is shown above.";

    private final PlannerService planner;
    private final GeminiClient gemini;
    private final PlanCache cache;

    public PlanController(PlannerService planner, GeminiClient gemini, PlanCache cache) {
        this.planner = planner;
        this.gemini = gemini;
        this.cache = cache;
    }

    @PostMapping("/plan")
    public PlanResponse plan(@Valid @RequestBody EventRequest request) {
        return cache.get(request).orElseGet(() -> buildPlan(request));
    }

    private PlanResponse buildPlan(EventRequest request) {
        PlanResponse draft = planner.plan(request);
        Optional<String> ai = gemini.summarize(request, draft);
        PlanResponse plan = draft.withAiSummary(ai.orElse(AI_FALLBACK));
        // Cache only successful AI results, so a temporary Gemini failure is not remembered.
        ai.ifPresent(summary -> cache.put(request, plan));
        return plan;
    }
}