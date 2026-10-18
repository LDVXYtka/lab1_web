package ru.itmo.students.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentPatchRequest(
        @Size(max = 100, message = StudentRules.NAME_LENGTH_MESSAGE)
        @Pattern(regexp = StudentRules.NAME, message = StudentRules.NAME_MESSAGE)
        String name,

        @Pattern(regexp = StudentRules.GROUP, message = StudentRules.GROUP_MESSAGE)
        String group,

        @Pattern(regexp = StudentRules.ISU, message = StudentRules.ISU_MESSAGE)
        String isu,

        @Min(value = 1, message = StudentRules.DORMITORY_MESSAGE)
        @Max(value = 8, message = StudentRules.DORMITORY_MESSAGE)
        Integer dormitory,

        @Pattern(regexp = StudentRules.ROOM, message = StudentRules.ROOM_MESSAGE)
        String room,

        LocalDate term,

        Boolean international,

        @Size(max = 2000, message = StudentRules.NOTE_MESSAGE)
        String note
) {
}
