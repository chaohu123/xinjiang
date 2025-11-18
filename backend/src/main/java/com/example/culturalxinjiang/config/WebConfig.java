package com.example.culturalxinjiang.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.static.digital-images-dir:digital-images}")
    private String digitalImagesDir;

    @Autowired
    private CultureTypeConverter cultureTypeConverter;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(buildFileUri(uploadDir));

        registry.addResourceHandler("/digital-images/**")
                .addResourceLocations(buildFileUri(digitalImagesDir));

        registry.addResourceHandler("/api/digital-images/**")
            .addResourceLocations(buildFileUri(digitalImagesDir));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(cultureTypeConverter);
    }

    private String buildFileUri(String directory) {
        Path path = Paths.get(directory).toAbsolutePath();
        String uri = path.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }
}



