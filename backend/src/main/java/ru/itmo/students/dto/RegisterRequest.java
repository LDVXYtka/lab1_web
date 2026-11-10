package ru.itmo.students.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotNull(message = "Обязательное поле")
        @Pattern(regexp = "[A-Za-z0-9_]{3,32}",
                message = "Имя пользователя: от 3 до 32 латинских букв, цифр или знаков _")
        String username,

        @NotNull(message = "Обязательное поле")
        @Size(max = 254, message = "Почта не длиннее 254 символов")
        @Email(regexp = ".+@.+\\..+", message = "Введите почту в формате name@example.com")
        String email,

        @NotNull(message = "Обязательное поле")
        @Size(min = 8, max = 72, message = "Пароль от 8 до 72 символов")
        @Pattern(regexp = "\\p{Graph}+",
                message = "Пароль: только латинские буквы, цифры и знаки, без пробелов")
        String password
) {
}
