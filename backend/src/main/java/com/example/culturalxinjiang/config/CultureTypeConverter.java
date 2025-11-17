package com.example.culturalxinjiang.config;

import com.example.culturalxinjiang.entity.CultureResource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CultureTypeConverter implements Converter<String, CultureResource.CultureType> {

    @Override
    public CultureResource.CultureType convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Culture type cannot be null or empty");
        }

        // 转换为大写并尝试匹配枚举
        try {
            return CultureResource.CultureType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid culture type: " + source + ". Valid types are: ARTICLE, EXHIBIT, VIDEO, AUDIO", e
            );
        }
    }
}





