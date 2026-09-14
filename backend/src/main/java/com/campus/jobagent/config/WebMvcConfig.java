package com.campus.jobagent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 把本地上传目录映射成静态资源，前端可直接通过 URL 预览简历附件。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.storage.local-path:./data/upload}")
    private String localPath;

    @Value("${app.storage.url-prefix:/files}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(localPath).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(urlPrefix + "/**").addResourceLocations(location);
    }
}
