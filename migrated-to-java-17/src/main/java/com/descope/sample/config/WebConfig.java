package com.descope.sample.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the Spring Boot application.
 * This replaces the RouteConfig.cs and BundleConfig.cs from the .NET application.
 * 
 * Migrated from:
 * - DescopeSampleApp/App_Start/RouteConfig.cs (URL routing)
 * - DescopeSampleApp/App_Start/BundleConfig.cs (static resource handling)
 * 
 * Spring Boot handles most of this automatically, but this class allows
 * for customization of view controllers and resource handlers.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map root URL to home view (equivalent to RouteConfig default route)
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/authenticated").setViewName("authenticated");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Static resource handling (equivalent to BundleConfig)
        // Spring Boot auto-configures /static, /public, /resources, /META-INF/resources
        // This is for any additional custom resource locations
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/");
    }
}
