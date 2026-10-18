package ru.itmo.students.dto;

import java.util.List;

public record StudentPage(
        List<StudentResponse> items,
        int total,
        int page,
        int size
) {
}
