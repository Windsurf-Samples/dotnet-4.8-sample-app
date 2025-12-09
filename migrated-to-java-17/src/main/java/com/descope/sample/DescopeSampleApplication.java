package com.descope.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class.
 * This replaces the Global.asax.cs Application_Start() method from .NET.
 * 
 * Spring Boot auto-configuration handles:
 * - Route registration (via @Controller and @RestController annotations)
 * - Filter registration (via @Component or SecurityFilterChain)
 * - Web API configuration (via application.properties/yml)
 */
@SpringBootApplication
public class DescopeSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(DescopeSampleApplication.class, args);
    }
}
