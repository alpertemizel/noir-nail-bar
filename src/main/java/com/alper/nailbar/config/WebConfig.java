package com.alper.nailbar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Tüm API endpoint'leri için CORS (Cross-Origin Resource Sharing) yapılandırması
// Frontend farklı bir porttan çalışsa bile API'ye erişebilir
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:8080",  // Spring Boot'un kendi static dosyaları
                        "http://localhost:5173",  // Vite dev server
                        "http://localhost:5500"   // VS Code Live Server
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
