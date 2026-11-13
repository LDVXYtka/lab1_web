package ru.itmo.students.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.students.dto.LoginRequest;
import ru.itmo.students.dto.RegisterRequest;
import ru.itmo.students.dto.TokenResponse;
import ru.itmo.students.dto.UserResponse;
import ru.itmo.students.service.AuthService;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Пользователи и вход")
public class AuthController {

    private final AuthService authService;
    private final boolean cookieSecure;
    private final long accessMinutes;
    private final long refreshDays;

    public AuthController(AuthService authService,
                          @Value("${cookie.secure}") boolean cookieSecure,
                          @Value("${jwt.access-minutes}") long accessMinutes,
                          @Value("${jwt.refresh-days}") long refreshDays) {
        this.authService = authService;
        this.cookieSecure = cookieSecure;
        this.accessMinutes = accessMinutes;
        this.refreshDays = refreshDays;
    }

    @GetMapping("/csrf")
    @Operation(summary = "CSRF-токен для заголовка запросов, меняющих данные")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @PostMapping("/csrf")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Проверка CSRF перед объединением параллельных запросов")
    public void checkCsrf() {
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация пользователя с ролью USER")
    public void register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход по имени и паролю, токены сохраняются в HttpOnly cookie")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        return tokenCookies(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Новая пара токенов по refresh-токену, старый refresh перестает работать")
    public ResponseEntity<Void> refresh(@CookieValue(name = "lab5RefreshToken", required = false) String refreshToken) {
        return tokenCookies(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход, refresh-токен перестает работать")
    public ResponseEntity<Void> logout(@CookieValue(name = "lab5RefreshToken", required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("lab5AccessToken", "", "/", Duration.ZERO).toString())
                .header(HttpHeaders.SET_COOKIE, cookie("lab5RefreshToken", "", "/", Duration.ZERO).toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь по access-токену")
    public UserResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }

    private ResponseEntity<Void> tokenCookies(TokenResponse tokens) {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        cookie("lab5AccessToken", tokens.accessToken(), "/", Duration.ofMinutes(accessMinutes)).toString())
                .header(HttpHeaders.SET_COOKIE,
                        cookie("lab5RefreshToken", tokens.refreshToken(), "/", Duration.ofDays(refreshDays)).toString())
                .build();
    }

    private ResponseCookie cookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}
