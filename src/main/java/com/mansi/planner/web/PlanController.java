package com.mansi.planner.web;

import com.mansi.planner.model.EventRequest;
import com.mansi.planner.model.PlanResponse;
import com.mansi.planner.service.GeminiClient;
import com.mansi.planner.service.PlannerService;
import jakarta.validation.Valid;
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

    public PlanController(PlannerService planner, GeminiClient gemini) {
        this.planner = planner;
        this.gemini = gemini;
    }

    @PostMapping("/plan")
    public PlanResponse plan(@Valid @RequestBody EventRequest request) {
        PlanResponse draft = planner.plan(request);
        return draft.withAiSummary(gemini.summarize(request, draft).orElse(AI_FALLBACK));
    }
}