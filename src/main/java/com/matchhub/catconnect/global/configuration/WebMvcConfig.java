package com.matchhub.catconnect.global.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.hero.image-dir:${file.upload-dir}/hero-images}")
    private String heroImageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/hero-images/**")
                .addResourceLocations("file:" + heroImageDir + "/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS));
    }
}
