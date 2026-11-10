package ru.itmo.students.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import ru.itmo.students.exception.ErrorResponse;
import ru.itmo.students.service.TokenService;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    private final JsonMapper jsonMapper;
    private final SecretKey secretKey;
    private final boolean cookieSecure;

    public SecurityConfig(JsonMapper jsonMapper, @Value("${jwt.secret}") String secret,
                          @Value("${cookie.secure}") boolean cookieSecure) {
        this.jsonMapper = jsonMapper;
        this.cookieSecure = cookieSecure;
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET должен быть не короче 32 байт в UTF-8");
        }
        this.secretKey = new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint unauthorized = (request, response, e) ->
                writeError(response, 401, "Нужно войти в аккаунт");
        CookieCsrfTokenRepository csrfTokens = new CookieCsrfTokenRepository();
        csrfTokens.setCookieCustomizer(cookie -> cookie.httpOnly(true).sameSite("Lax").secure(cookieSecure));
        JwtAuthenticationProvider jwtProvider = new JwtAuthenticationProvider(jwtDecoder());
        jwtProvider.setJwtAuthenticationConverter(rolesConverter());
        BearerTokenAuthenticationConverter tokenConverter = new BearerTokenAuthenticationConverter();
        tokenConverter.setBearerTokenResolver(this::accessToken);
        BearerTokenAuthenticationFilter jwtFilter =
                new BearerTokenAuthenticationFilter(new ProviderManager(jwtProvider), tokenConverter);
        jwtFilter.setAuthenticationEntryPoint(unauthorized);
        http
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokens)
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login",
                                "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/requests/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.valueOf("QUERY"), "/api/requests").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler((request, response, e) ->
                                writeError(response, 403, e instanceof CsrfException
                                        ? "Неверный CSRF-токен, обновите страницу"
                                        : "Недостаточно прав для этого действия")));
        return http.build();
    }

    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowedHttpMethods(List.of("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "QUERY"));
        return firewall;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.ISSUER));
        return decoder;
    }

    private JwtAuthenticationConverter rolesConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    private String accessToken(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || List.of("/api/auth/csrf", "/api/auth/login", "/api/auth/register",
                "/api/auth/refresh", "/api/auth/logout").contains(path)) {
            return null;
        }
        String bearerToken = new DefaultBearerTokenResolver().resolve(request);
        if (bearerToken != null) {
            return bearerToken;
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        jsonMapper.writeValue(response.getOutputStream(), new ErrorResponse(status, message, Map.of()));
    }
}
