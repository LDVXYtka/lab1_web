package ru.itmo.students.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class StudentPatchRequest {

    @JsonSetter(nulls = Nulls.FAIL)
    @Size(max = 100, message = StudentRules.NAME_LENGTH_MESSAGE)
    @Pattern(regexp = StudentRules.NAME, message = StudentRules.NAME_MESSAGE)
    public String name;

    @JsonSetter(nulls = Nulls.FAIL)
    @Pattern(regexp = StudentRules.GROUP, message = StudentRules.GROUP_MESSAGE)
    public String group;

    @JsonSetter(nulls = Nulls.FAIL)
    @Pattern(regexp = StudentRules.ISU, message = StudentRules.ISU_MESSAGE)
    public String isu;

    @JsonSetter(nulls = Nulls.FAIL)
    @Min(value = 1, message = StudentRules.DORMITORY_MESSAGE)
    @Max(value = 8, message = StudentRules.DORMITORY_MESSAGE)
    public Integer dormitory;

    @JsonSetter(nulls = Nulls.FAIL)
    @Pattern(regexp = StudentRules.ROOM, message = StudentRules.ROOM_MESSAGE)
    public String room;

    @JsonSetter(nulls = Nulls.FAIL)
    public LocalDate term;

    @JsonSetter(nulls = Nulls.FAIL)
    public Boolean international;

    @JsonSetter(nulls = Nulls.FAIL)
    @Size(max = 2000, message = StudentRules.NOTE_MESSAGE)
    public String note;
}
