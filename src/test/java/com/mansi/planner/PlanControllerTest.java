package com.mansi.planner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mansi.planner.service.GeminiClient;
import com.mansi.planner.service.PlanCache;
import com.mansi.planner.service.PlannerService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import({PlannerService.class, PlanCache.class})
class PlanControllerTest {

    @Autowired MockMvc mvc;
    @MockBean GeminiClient gemini;

    private String json(String type, long budget) {
        return """
            {"type":"%s","date":"%s","budget":%d,"guests":100,"city":"Pune",
             "outdoor":false,"diet":"VEG","language":"ENGLISH","notes":"<script>alert(1)</script>"}
            """.formatted(type, LocalDate.now().plusDays(40), budget);
    }

    @Test
    void validRequestReturnsPlan() throws Exception {
        when(gemini.summarize(any(), any())).thenReturn(Optional.of("AI summary"));
        mvc.perform(post("/api/plan").contentType(MediaType.APPLICATION_JSON)
                        .content(json("WEDDING", 500000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiSummary").value("AI summary"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void geminiFailureStillReturnsRuleBasedPlan() throws Exception {
        when(gemini.summarize(any(), any())).thenReturn(Optional.empty());
        mvc.perform(post("/api/plan").contentType(MediaType.APPLICATION_JSON)
                        .content(json("BIRTHDAY", 100000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checklist").isNotEmpty());
    }

    @Test
    void negativeBudgetIsRejected() throws Exception {
        mvc.perform(post("/api/plan").contentType(MediaType.APPLICATION_JSON)
                        .content(json("WEDDING", -5)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownEventTypeIsRejected() throws Exception {
        mvc.perform(post("/api/plan").contentType(MediaType.APPLICATION_JSON)
                        .content(json("HACKATHON", 100000)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void identicalRequestIsServedFromCache() throws Exception {
        when(gemini.summarize(any(), any())).thenReturn(Optional.of("cached summary"));
        String body = json("CORPORATE", 250000);
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/plan").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.aiSummary").value("cached summary"));
        }
        verify(gemini, times(1)).summarize(any(), any());
    }
}