package ru.itmo.students.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
