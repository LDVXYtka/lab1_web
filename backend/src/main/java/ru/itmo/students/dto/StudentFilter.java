package ru.itmo.students.dto;

import java.time.LocalDate;

public record StudentFilter(
        String name,
        String group,
        String isu,
        Integer dormitory,
        String room,
        LocalDate termFrom,
        LocalDate termTo,
        Boolean international
) {
}
