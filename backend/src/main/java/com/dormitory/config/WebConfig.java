package com.dormitory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类 - 配置静态资源访问
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        // 访问路径：http://localhost:8080/uploads/文件名
        // 实际路径：项目根目录/uploads/文件名
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

