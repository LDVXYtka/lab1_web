package ru.itmo.students.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record StudentQuery(
        String name,
        String group,
        String isu,
        Integer dormitory,
        String room,
        @JsonProperty("term_from") LocalDate termFrom,
        @JsonProperty("term_to") LocalDate termTo,
        Boolean international,
        Integer page,
        Integer size,
        String sort,
        String order
) {
}
