package ru.itmo.students.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        SecurityScheme accessCookie = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("accessToken")
                .description("Cookie устанавливается после входа");
        SecurityScheme csrf = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-XSRF-TOKEN")
                .description("Значение token из GET /api/auth/csrf для запросов, меняющих данные");
        return new OpenAPI()
                .info(new Info().title("Students API").version("4.0"))
                .components(new Components().addSecuritySchemes("accessCookie", accessCookie)
                        .addSecuritySchemes("csrf", csrf));
    }

    @Bean
    public OpenApiCustomizer securityRequirements() {
        return api -> api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
            boolean publicAuth = List.of("/api/auth/csrf", "/api/auth/register", "/api/auth/login",
                    "/api/auth/refresh", "/api/auth/logout").contains(path);
            SecurityRequirement requirement = new SecurityRequirement();
            if (!publicAuth) {
                requirement.addList("accessCookie");
            }
            if (List.of("POST", "PUT", "PATCH", "DELETE").contains(method.name())) {
                requirement.addList("csrf");
            }
            operation.setSecurity(requirement.isEmpty() ? List.of() : List.of(requirement));
        }));
    }
}
