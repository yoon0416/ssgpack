package com.ssgpack.ssgfc.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ✅ String -> LocalDateTime 변환기
@Component
public class LocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
}
