package ru.itmo.students.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.students.dto.LoginRequest;
import ru.itmo.students.dto.RefreshRequest;
import ru.itmo.students.dto.RegisterRequest;
import ru.itmo.students.dto.TokenResponse;
import ru.itmo.students.dto.UserResponse;
import ru.itmo.students.exception.ConflictException;
import ru.itmo.students.exception.NotFoundException;
import ru.itmo.students.exception.UnauthorizedException;
import ru.itmo.students.jooq.tables.records.RefreshTokensRecord;
import ru.itmo.students.jooq.tables.records.UsersRecord;
import ru.itmo.students.repository.RefreshTokenRepository;
import ru.itmo.students.repository.UserRepository;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final TokenService tokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository users,
                       RefreshTokenRepository refreshTokens,
                       TokenService tokens,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        Map<String, String> fields = new LinkedHashMap<>();
        if (users.existsByUsername(request.username())) {
            fields.put("username", "Это имя пользователя уже занято");
        }
        if (users.existsByEmail(email)) {
            fields.put("email", "Эта почта уже зарегистрирована");
        }
        if (!fields.isEmpty()) {
            throw new ConflictException("Пользователь с такими данными уже есть", fields);
        }
        String passwordHash = passwordEncoder.encode(request.password());
        UsersRecord user = users.insert(request.username(), email, passwordHash, request.role());
        log.info("Зарегистрирован пользователь {} с ролью {}", user.getUsername(), user.getRole());
        return toResponse(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException e) {
            log.warn("Неудачный вход: {}", request.username());
            throw new UnauthorizedException("Неверное имя пользователя или пароль");
        }
        UsersRecord user = users.findByUsername(request.username()).orElseThrow();
        log.info("Вход пользователя {}", user.getUsername());
        return tokens.issue(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        RefreshTokensRecord old = refreshTokens.findValid(tokens.hash(request.refreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Refresh-токен недействителен или истек"));
        refreshTokens.delete(old.getId());
        UsersRecord user = users.findById(old.getUserId()).orElseThrow();
        log.info("Обновлены токены пользователя {}", user.getUsername());
        return tokens.issue(user);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokens.findValid(tokens.hash(request.refreshToken())).ifPresent(token -> {
            refreshTokens.delete(token.getId());
            log.info("Выход пользователя с id {}", token.getUserId());
        });
    }

    public UserResponse me(String username) {
        UsersRecord user = users.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return toResponse(user);
    }

    private UserResponse toResponse(UsersRecord user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
