package ru.itmo.students.repository;

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
