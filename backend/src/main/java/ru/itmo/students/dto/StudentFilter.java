package ru.itmo.students.dto;

import java.time.LocalDate;

public record StudentFilter(
        Long id,
        String name,
        String group,
        String isu,
        Integer dormitory,
        String room,
        LocalDate termFrom,
        LocalDate termTo,
        Boolean international,
        String note
) {
}
