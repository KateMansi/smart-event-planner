package com.mansi.planner.model;

import java.util.List;
import java.util.Map;

public record PlanResponse(
        String mode,
        long perGuestBudget,
        Map<String, Long> budget,
        List<String> checklist,
        List<String> warnings,
        String aiSummary) {

    public PlanResponse withAiSummary(String summary) {
        return new PlanResponse(mode, perGuestBudget, budget, checklist, warnings, summary);
    }
}