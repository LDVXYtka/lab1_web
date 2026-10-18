package ru.itmo.students.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "Обязательное поле")
        String username,

        @NotNull(message = "Обязательное поле")
        String password
) {
}
