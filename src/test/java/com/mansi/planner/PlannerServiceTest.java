package com.mansi.planner;

import static org.junit.jupiter.api.Assertions.*;

import com.mansi.planner.model.EventRequest;
import com.mansi.planner.model.EventRequest.*;
import com.mansi.planner.model.PlanResponse;
import com.mansi.planner.service.PlannerService;
import java.time.*;
import org.junit.jupiter.api.Test;

class PlannerServiceTest {

    private final Clock fixed = Clock.fixed(Instant.parse("2026-11-01T00:00:00Z"), ZoneOffset.UTC);
    private final PlannerService service = new PlannerService(fixed);

    private EventRequest req(LocalDate date, long budget, int guests, boolean outdoor, Diet diet) {
        return new EventRequest(EventType.WEDDING, date, budget, guests, "Pune",
                outdoor, diet, Language.ENGLISH, null);
    }

    @Test
    void urgentModeWhenEventIsWithinTwoWeeks() {
        PlanResponse p = service.plan(req(LocalDate.of(2026, 11, 5), 500_000, 100, false, Diet.VEG));
        assertEquals("URGENT", p.mode());
    }

    @Test
    void standardModeWhenEnoughTime() {
        PlanResponse p = service.plan(req(LocalDate.of(2026, 12, 20), 500_000, 100, false, Diet.VEG));
        assertEquals("STANDARD", p.mode());
    }

    @Test
    void lowBudgetPerGuestShiftsMoneyFromDecorToCatering() {
        PlanResponse normal = service.plan(req(LocalDate.of(2026, 12, 20), 500_000, 100, false, Diet.VEG));
        PlanResponse low = service.plan(req(LocalDate.of(2026, 12, 20), 20_000, 100, false, Diet.VEG));
        assertTrue(low.warnings().stream().anyMatch(w -> w.contains("per guest")));
        assertTrue(low.budget().get("Catering") * 100 / 20_000
                > normal.budget().get("Catering") * 100 / 500_000);
    }

    @Test
    void budgetBreakdownAlwaysSumsToTotal() {
        PlanResponse p = service.plan(req(LocalDate.of(2026, 12, 20), 333_333, 77, false, Diet.JAIN));
        assertEquals(333_333L, p.budget().values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void largeEventAddsSecurityTask() {
        PlanResponse p = service.plan(req(LocalDate.of(2026, 12, 20), 900_000, 300, false, Diet.VEG));
        assertTrue(p.checklist().stream().anyMatch(c -> c.contains("security")));
    }

    @Test
    void outdoorMonsoonEventGetsBackupPlan() {
        PlanResponse p = service.plan(req(LocalDate.of(2027, 7, 10), 500_000, 100, true, Diet.VEG));
        assertTrue(p.warnings().stream().anyMatch(w -> w.contains("monsoon")));
    }

    @Test
    void jainDietAddsJainMenuItem() {
        PlanResponse p = service.plan(req(LocalDate.of(2026, 12, 20), 500_000, 100, false, Diet.JAIN));
        assertTrue(p.checklist().stream().anyMatch(c -> c.contains("Jain")));
    }
}