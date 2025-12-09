# Task B1: Maven Project Setup

## Executive Summary

This document details the creation of the Spring Boot Maven project structure that serves as the foundation for the migrated Descope Sample Application. The project was initialized following Spring Boot 3.2 best practices and Maven conventions.

## Project Overview

| Property | Value |
|----------|-------|
| Group ID | com.descope |
| Artifact ID | descope-sample-app |
| Version | 1.0.0-SNAPSHOT |
| Java Version | 17 |
| Spring Boot Version | 3.2.0 |
| Build Tool | Maven 3.x |

## Directory Structure Created

```
migrated-to-java-17/
├── pom.xml                                    # Maven build configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── descope/
│   │   │           └── sample/
│   │   │               ├── DescopeSampleApplication.java    # Main class
│   │   │               ├── config/                          # Configuration classes
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── OpenApiConfig.java
│   │   │               │   └── WebConfig.java
│   │   │               ├── controller/                      # Web controllers
│   │   │               │   ├── HomeController.java
│   │   │               │   └── SampleApiController.java
│   │   │               └── security/                        # Security components
│   │   │                   └── TokenValidator.java
│   │   └── resources/
│   │       ├── application.properties                       # Configuration
│   │       ├── templates/                                   # Thymeleaf templates
│   │       │   ├── home.html
│   │       │   ├── login.html
│   │       │   └── authenticated.html
│   │       └── static/                                      # Static resources
│   │           └── css/
│   └── test/
│       └── java/
│           └── com/
│               └── descope/
│                   └── sample/
│                       └── DescopeSampleApplicationTests.java
└── target/                                    # Build output (gitignored)
```

## Main Application Class

### Source (.NET): Global.asax.cs

```csharp
public class WebApiApplication : System.Web.HttpApplication
{
    protected void Application_Start()
    {
        AreaRegistration.RegisterAllAreas();
        GlobalConfiguration.Configure(WebApiConfig.Register);
        FilterConfig.RegisterGlobalFilters(GlobalFilters.Filters);
        RouteConfig.RegisterRoutes(RouteTable.Routes);
        BundleConfig.RegisterBundles(BundleTable.Bundles);
    }
}
```

### Target (Spring Boot): DescopeSampleApplication.java

```java
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
```

## Startup Sequence Comparison

### .NET Framework 4.8 Startup

```
Application_Start()
        │
        ▼
┌───────────────────────────────────────┐
│ 1. AreaRegistration.RegisterAllAreas()│
│    - Registers HelpPage area          │
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│ 2. WebApiConfig.Register()            │
│    - Enables attribute routing        │
│    - Maps api/{controller}/{id}       │
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│ 3. FilterConfig.RegisterGlobalFilters()│
│    - Adds HandleErrorAttribute        │
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│ 4. RouteConfig.RegisterRoutes()       │
│    - Maps {controller}/{action}/{id}  │
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│ 5. BundleConfig.RegisterBundles()     │
│    - ~/bundles/jquery                 │
│    - ~/bundles/bootstrap              │
└───────────────────────────────────────┘
```

### Spring Boot 3.2 Startup

```
SpringApplication.run()
        │
        ▼
┌───────────────────────────────────────┐
│ 1. @SpringBootApplication             │
│    - Component scanning               │
│    - Auto-configuration               │
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│ 2. @Configuration classes loaded      │
│    - SecurityConfig                   │
│    - OpenApiConfig                    │
│    - WebConfig                        │
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│ 3. @Controller/@RestController scanned│
│    - HomeController                   │
│    - SampleApiController              │
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│ 4. SecurityFilterChain configured     │
│    - JWT validation                   │
│    - Route protection                 │
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│ 5. Embedded Tomcat started            │
│    - Port 8080                        │
│    - Static resources served          │
└───────────────────────────────────────┘
```

## Key Differences

| Aspect | .NET Framework 4.8 | Spring Boot 3.2 |
|--------|-------------------|-----------------|
| Entry Point | `Global.asax.cs` | `@SpringBootApplication` class |
| Configuration | Explicit method calls | Auto-configuration + annotations |
| Routing | `RouteConfig.cs` | `@RequestMapping` annotations |
| Filters | `FilterConfig.cs` | `SecurityFilterChain` bean |
| Bundling | `BundleConfig.cs` | CDN or WebJars |
| Server | IIS | Embedded Tomcat |

## Package Structure Design

The package structure follows Spring Boot conventions:

```
com.descope.sample
├── DescopeSampleApplication.java    # Application entry point
├── config/                          # Configuration classes
│   ├── SecurityConfig.java          # Spring Security configuration
│   ├── OpenApiConfig.java           # Swagger/OpenAPI configuration
│   └── WebConfig.java               # Web MVC configuration
├── controller/                      # Web layer
│   ├── HomeController.java          # MVC controller for views
│   └── SampleApiController.java     # REST API controller
├── security/                        # Security components
│   └── TokenValidator.java          # JWT validation utility
├── service/                         # Business logic (future)
└── model/                           # Data models/DTOs (future)
```

## Maven POM Structure

The `pom.xml` follows the standard Spring Boot parent POM pattern:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.descope</groupId>
    <artifactId>descope-sample-app</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>Descope Sample App</name>
    <description>Spring Boot migration of .NET Framework 4.8 Descope Sample Application</description>

    <properties>
        <java.version>17</java.version>
        <!-- Additional version properties -->
    </properties>

    <dependencies>
        <!-- Dependencies defined in B2 -->
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Build Commands

| Command | Purpose |
|---------|---------|
| `mvn compile` | Compile the project |
| `mvn test` | Run unit tests |
| `mvn package` | Create JAR file |
| `mvn spring-boot:run` | Run the application |
| `mvn clean install` | Full build with tests |

## Verification

The project structure was verified by:

1. **Compilation**: `mvn compile` completed successfully
2. **Tests**: `mvn test` passed all tests
3. **Application Startup**: Application starts on port 8080

## Summary

Task B1 established the foundational Maven project structure for the Spring Boot migration. The project follows Spring Boot conventions and provides a clean separation of concerns through the package structure. The `@SpringBootApplication` annotation replaces the explicit startup configuration from `Global.asax.cs`, leveraging Spring Boot's auto-configuration capabilities.
