package ru.itmo.students.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import ru.itmo.students.exception.BadRequestException;

import java.time.LocalDate;

public record StudentQuery(
        Long id,
        String name,
        String group,
        String isu,
        Integer dormitory,
        String room,
        @JsonProperty("term_from") LocalDate termFrom,
        @JsonProperty("term_to") LocalDate termTo,
        Boolean international,
        String note,
        Integer page,
        Integer size,
        String sort,
        String order
) {

    @JsonAnySetter
    public void rejectUnknown(String name, Object value) {
        throw new BadRequestException("Неизвестный фильтр: " + name);
    }
}
