package ru.itmo.students.dto;

import jakarta.validation.constraints.NotNull;

public record RefreshRequest(
        @NotNull(message = "Обязательное поле")
        String refreshToken
) {
}
