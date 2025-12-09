package com.descope.sample.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * This replaces the ASP.NET Help Page system from the .NET application.
 * 
 * Migrated from: DescopeSampleApp/Areas/HelpPage/
 * 
 * The ASP.NET Help Page system provided:
 * - Auto-discovery of API endpoints
 * - Model description generation
 * - Sample request/response generation
 * 
 * Springdoc OpenAPI provides equivalent functionality through:
 * - Automatic endpoint scanning via @RestController annotations
 * - Schema generation from Java classes
 * - Example generation via @Schema annotations
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Descope Sample API")
                .version("1.0.0")
                .description("Spring Boot migration of .NET Framework 4.8 Descope Sample Application. " +
                    "This API demonstrates JWT authentication with Descope integration.")
                .contact(new Contact()
                    .name("Descope Sample Apps")
                    .url("https://github.com/descope-sample-apps"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter your JWT token obtained from Descope authentication")));
    }
}
