package ru.itmo.students.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.students.dto.LoginRequest;
import ru.itmo.students.dto.RefreshRequest;
import ru.itmo.students.dto.RegisterRequest;
import ru.itmo.students.dto.TokenResponse;
import ru.itmo.students.dto.UserResponse;
import ru.itmo.students.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Пользователи и вход")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация, новый пользователь получает роль USER")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход по имени и паролю, выдает access и refresh токены")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Новая пара токенов по refresh-токену, старый refresh перестает работать")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь по access-токену")
    public UserResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }
}
