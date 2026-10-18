package ru.itmo.students.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentRequest(
        @NotNull(message = StudentRules.REQUIRED)
        @Size(max = 100, message = StudentRules.NAME_LENGTH_MESSAGE)
        @Pattern(regexp = StudentRules.NAME, message = StudentRules.NAME_MESSAGE)
        String name,

        @NotNull(message = StudentRules.REQUIRED)
        @Pattern(regexp = StudentRules.GROUP, message = StudentRules.GROUP_MESSAGE)
        String group,

        @NotNull(message = StudentRules.REQUIRED)
        @Pattern(regexp = StudentRules.ISU, message = StudentRules.ISU_MESSAGE)
        String isu,

        @NotNull(message = StudentRules.REQUIRED)
        @Min(value = 1, message = StudentRules.DORMITORY_MESSAGE)
        @Max(value = 8, message = StudentRules.DORMITORY_MESSAGE)
        Integer dormitory,

        @NotNull(message = StudentRules.REQUIRED)
        @Pattern(regexp = StudentRules.ROOM, message = StudentRules.ROOM_MESSAGE)
        String room,

        @NotNull(message = StudentRules.REQUIRED)
        LocalDate term,

        @NotNull(message = StudentRules.REQUIRED)
        Boolean international,

        @Size(max = 2000, message = StudentRules.NOTE_MESSAGE)
        String note
) {
}
