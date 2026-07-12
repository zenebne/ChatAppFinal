package com.example.chatapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * Verantwortungsbereich: Backend-Konfiguration und Integration.
 *
 * @author Zeynep Ünver
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = uploadDir;
        if (!path.endsWith("/")) {
            path += "/";
        }
        
        // Add file: prefix if not present
        if (!path.startsWith("file:")) {
            path = "file:" + path;
        }

        registry.addResourceHandler("/media/**")
                .addResourceLocations(path);
    }
}
