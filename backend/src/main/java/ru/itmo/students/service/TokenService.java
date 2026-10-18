package ru.itmo.students.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.itmo.students.dto.TokenResponse;
import ru.itmo.students.jooq.tables.records.UsersRecord;
import ru.itmo.students.repository.RefreshTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class TokenService {

    public static final String ISSUER = "students-app";

    private final JwtEncoder encoder;
    private final RefreshTokenRepository refreshTokens;
    private final long accessMinutes;
    private final long refreshDays;
    private final SecureRandom random = new SecureRandom();

    public TokenService(JwtEncoder encoder,
                        RefreshTokenRepository refreshTokens,
                        @Value("${jwt.access-minutes}") long accessMinutes,
                        @Value("${jwt.refresh-days}") long refreshDays) {
        if (accessMinutes <= 0 || refreshDays <= 0) {
            throw new IllegalStateException("JWT_ACCESS_MINUTES и JWT_REFRESH_DAYS должны быть больше 0");
        }
        this.encoder = encoder;
        this.refreshTokens = refreshTokens;
        this.accessMinutes = accessMinutes;
        this.refreshDays = refreshDays;
    }

    public TokenResponse issue(UsersRecord user) {
        return new TokenResponse(createAccessToken(user), createRefreshToken(user));
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String createAccessToken(UsersRecord user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(accessMinutes, ChronoUnit.MINUTES))
                .claim("roles", List.of(user.getRole()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String createRefreshToken(UsersRecord user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokens.insert(user.getId(), hash(token), OffsetDateTime.now().plusDays(refreshDays));
        return token;
    }
}
