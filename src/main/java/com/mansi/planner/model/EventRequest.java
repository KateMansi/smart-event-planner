
package com.mansi.planner.model;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record EventRequest(
        @NotNull EventType type,
        @NotNull @FutureOrPresent LocalDate date,
        @Min(1_000) @Max(100_000_000) long budget,
        @Min(1) @Max(5_000) int guests,
        @NotBlank @Size(max = 60) String city,
        boolean outdoor,
        @NotNull Diet diet,
        @NotNull Language language,
        @Size(max = 500) String notes) {

    public enum EventType { WEDDING, BIRTHDAY, CORPORATE, PARTY }
    public enum Diet { VEG, NON_VEG, JAIN, HALAL }
    public enum Language { ENGLISH, HINDI, MARATHI }
}