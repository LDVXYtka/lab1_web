package ru.itmo.students.dto;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String name,
        String group,
        String isu,
        Integer dormitory,
        String room,
        LocalDate term,
        Boolean international,
        String note
) {
}
