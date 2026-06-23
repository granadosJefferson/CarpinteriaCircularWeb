package com.carpinteria.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path rutaImagenes = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/uploads/productos/**")
                .addResourceLocations(rutaImagenes.toUri().toString());
    }
}