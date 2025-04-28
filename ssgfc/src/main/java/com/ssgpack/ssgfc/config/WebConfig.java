package com.ssgpack.ssgfc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LocalDateTimeConverter localDateTimeConverter; // ✅ 변환기 주입

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${resource.path}")
    private String resourcePath;

    // ✅ 정적 리소스 경로 매핑
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadPath)
                .addResourceLocations("file:" + resourcePath);
    }

    // ✅ 단순 URL 매핑 (뷰 컨트롤러)
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/popup").setViewName("popup/popup");
    }

    // ✅ LocalDateTime 변환 포맷 추가 (datetime-local 입력값 매칭)
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(localDateTimeConverter);
    }
}
