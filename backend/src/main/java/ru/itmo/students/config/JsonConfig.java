package ru.itmo.students.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.type.LogicalType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Configuration
public class JsonConfig {

    @Bean
    public JsonMapperBuilderCustomizer strictJson() {
        SimpleModule dates = new SimpleModule();
        dates.addDeserializer(LocalDate.class, new ValueDeserializer<>() {
            @Override
            public LocalDate deserialize(JsonParser parser, DeserializationContext context) {
                if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                    return context.reportInputMismatch(LocalDate.class, "Дата должна быть строкой ГГГГ-ММ-ДД");
                }
                String value = parser.getString();
                if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                    return context.reportInputMismatch(LocalDate.class, "Дата должна быть строкой ГГГГ-ММ-ДД");
                }
                try {
                    return LocalDate.parse(value);
                } catch (DateTimeParseException e) {
                    return context.reportInputMismatch(LocalDate.class, "Укажите существующую дату");
                }
            }
        });
        return builder -> builder.addModule(dates).withCoercionConfig(LogicalType.Textual, config -> config
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
    }
}
