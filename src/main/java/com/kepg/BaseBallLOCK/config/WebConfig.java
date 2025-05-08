package com.kepg.BaseBallLOCK.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kepg.BaseBallLOCK.common.FileManager;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
        .addResourceLocations("file://" + FileManager.FILE_UPLOAD_PATH + "/");
    }
}