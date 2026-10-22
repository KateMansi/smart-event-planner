package com.mansi.planner.service;

import com.mansi.planner.model.EventRequest;
import com.mansi.planner.model.EventRequest.Diet;
import com.mansi.planner.model.EventRequest.EventType;
import com.mansi.planner.model.PlanResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PlannerService {

    static final int URGENT_DAYS = 14;
    static final long MIN_PER_GUEST = 500;
    static final int LARGE_EVENT_GUESTS = 200;
    static final int BUDGET_SHIFT_PERCENT = 10;

    private final Clock clock;

    public PlannerService(Clock clock) {
        this.clock = clock;
    }

    public PlanResponse plan(EventRequest req) {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(clock), req.date());
        boolean urgent = daysLeft < URGENT_DAYS;
        long perGuest = req.budget() / req.guests();
        boolean lowBudget = perGuest < MIN_PER_GUEST;

        List<String> warnings = new ArrayList<>();
        List<String> checklist = baseChecklist(req.type());

        if (urgent) {
            warnings.add("Only " + daysLeft + " days left: book venue and caterer first.");
            checklist.add(0, "Confirm venue and caterer within 48 hours");
        }
        if (lowBudget) {
            warnings.add("Budget per guest is Rs " + perGuest
                    + ". Catering is prioritised and decor is reduced.");
        }
        if (req.guests() > LARGE_EVENT_GUESTS) {
            checklist.add("Arrange parking, crowd control and security staff");
        }
        if (req.outdoor() && isRainySeason(req.date().getMonth())) {
            warnings.add("Outdoor event in monsoon season: arrange a covered backup venue.");
            checklist.add("Book a tent or indoor backup space");
        }
        addDietItems(req.diet(), checklist);

        return new PlanResponse(urgent ? "URGENT" : "STANDARD", perGuest,
                splitBudget(req, lowBudget), checklist, warnings, null);
    }

    private Map<String, Long> splitBudget(EventRequest req, boolean lowBudget) {
        Map<String, Integer> percent = new LinkedHashMap<>(basePercent(req.type()));
        if (lowBudget) {
            int shift = Math.min(BUDGET_SHIFT_PERCENT, percent.get("Decor"));
            percent.merge("Decor", -shift, Integer::sum);
            percent.merge("Catering", shift, Integer::sum);
        }
        Map<String, Long> amounts = new LinkedHashMap<>();
        long used = 0;
        for (var entry : percent.entrySet()) {
            long amount = req.budget() * entry.getValue() / 100;
            amounts.put(entry.getKey(), amount);
            used += amount;
        }
        amounts.put("Contingency", req.budget() - used);
        return amounts;
    }

    private Map<String, Integer> basePercent(EventType type) {
        Map<String, Integer> m = new LinkedHashMap<>();
        switch (type) {
            case WEDDING -> { m.put("Venue", 30); m.put("Catering", 35); m.put("Decor", 15);
                              m.put("Photography", 10); m.put("Entertainment", 5); }
            case BIRTHDAY -> { m.put("Venue", 20); m.put("Catering", 40); m.put("Decor", 20);
                               m.put("Cake and gifts", 10); m.put("Entertainment", 5); }
            case CORPORATE -> { m.put("Venue", 35); m.put("Catering", 35); m.put("Decor", 5);
                                m.put("AV and tech", 15); m.put("Entertainment", 5); }
            case PARTY -> { m.put("Venue", 25); m.put("Catering", 35); m.put("Decor", 15);
                            m.put("Music and DJ", 15); m.put("Entertainment", 5); }
        }
        return m;
    }

    private List<String> baseChecklist(EventType type) {
        List<String> list = new ArrayList<>(List.of(
                "Finalise guest list", "Book venue", "Select caterer and menu",
                "Send invitations", "Plan day-of schedule"));
        if (type == EventType.WEDDING) list.add("Book photographer and decorator");
        if (type == EventType.CORPORATE) list.add("Test projector, mic and Wi-Fi");
        return list;
    }

    private void addDietItems(Diet diet, List<String> checklist) {
        switch (diet) {
            case JAIN -> checklist.add("Confirm Jain menu (no onion, garlic, root vegetables)");
            case HALAL -> checklist.add("Confirm halal-certified caterer");
            case VEG -> checklist.add("Confirm fully vegetarian kitchen");
            case NON_VEG -> checklist.add("Offer a vegetarian option for guests who need it");
        }
    }

    private boolean isRainySeason(Month m) {
        return m.getValue() >= Month.JUNE.getValue() && m.getValue() <= Month.SEPTEMBER.getValue();
    }
}