# Task B2: Spring Boot Dependencies Configuration

## Executive Summary

This document details the Maven dependencies configured for the Spring Boot migration of the Descope Sample Application. Each dependency is mapped from its .NET equivalent with version specifications and migration notes.

## Dependency Overview

### Version Properties

```xml
<properties>
    <java.version>17</java.version>
    <springdoc.version>2.3.0</springdoc.version>
    <java-jwt.version>4.4.0</java-jwt.version>
    <jwks-rsa.version>0.22.1</jwks-rsa.version>
</properties>
```

## Complete Dependency Mapping

### 1. Spring Boot Web Starter

**Replaces:** Microsoft.AspNet.Mvc, Microsoft.AspNet.WebApi, Microsoft.AspNet.WebApi.Core

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

| Feature | .NET Package | Spring Boot |
|---------|-------------|-------------|
| MVC Controllers | Microsoft.AspNet.Mvc 5.2.9 | Included |
| REST Controllers | Microsoft.AspNet.WebApi 5.2.9 | Included |
| JSON Processing | Newtonsoft.Json 13.0.3 | Jackson (included) |
| HTTP Server | IIS | Embedded Tomcat |

**Migration Notes:**
- Spring Boot combines MVC and REST API in a single starter
- Jackson is the default JSON library (replaces Newtonsoft.Json)
- Embedded Tomcat eliminates need for external web server

### 2. Spring Boot Security Starter

**Replaces:** Manual authentication in controllers

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

| Feature | .NET Approach | Spring Security |
|---------|--------------|-----------------|
| Authentication | Manual in controller | Filter chain |
| Authorization | Manual checks | `@PreAuthorize` or config |
| CSRF Protection | `[ValidateAntiForgeryToken]` | Built-in |
| Session Management | ASP.NET Session | Configurable |

**Migration Notes:**
- Provides declarative security configuration
- Integrates with OAuth2 Resource Server for JWT
- Replaces manual token validation in controllers

### 3. Spring Boot OAuth2 Resource Server

**Replaces:** System.IdentityModel.Tokens.Jwt, Microsoft.IdentityModel.Tokens

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

| Feature | .NET Package | Spring OAuth2 |
|---------|-------------|---------------|
| JWT Validation | System.IdentityModel.Tokens.Jwt 7.5.1 | Built-in |
| JWKS Fetching | Manual HttpClient | Automatic |
| Token Parsing | jose-jwt 5.0.0 | NimbusJwtDecoder |
| Claim Extraction | Manual | `@AuthenticationPrincipal` |

**Migration Notes:**
- Automatic JWKS endpoint fetching and caching
- Built-in JWT signature validation
- Integrates seamlessly with Spring Security filter chain

### 4. Spring Boot Thymeleaf Starter

**Replaces:** Microsoft.AspNet.Razor, Microsoft.AspNet.WebPages

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

| Feature | Razor | Thymeleaf |
|---------|-------|-----------|
| Syntax | `@ViewBag.Property` | `${property}` |
| Conditionals | `@if (condition)` | `th:if="${condition}"` |
| Loops | `@foreach` | `th:each` |
| Links | `@Html.ActionLink()` | `th:href="@{/path}"` |
| Security Integration | Manual | `sec:authorize` |

**Migration Notes:**
- Thymeleaf uses natural templates (valid HTML)
- Spring Security integration via extras library
- Templates located in `src/main/resources/templates/`

### 5. Jackson JSON Library

**Replaces:** Newtonsoft.Json, System.Text.Json

```xml
<!-- Included in spring-boot-starter-web -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

| Feature | Newtonsoft.Json | Jackson |
|---------|----------------|---------|
| Serialization | `JsonConvert.SerializeObject()` | `ObjectMapper.writeValueAsString()` |
| Deserialization | `JsonConvert.DeserializeObject<T>()` | `ObjectMapper.readValue()` |
| Property Naming | `[JsonProperty]` | `@JsonProperty` |
| Ignore Null | `NullValueHandling.Ignore` | `@JsonInclude(NON_NULL)` |

**Migration Notes:**
- Jackson is Spring Boot's default JSON library
- Most Newtonsoft.Json features have Jackson equivalents
- Configured via `application.properties`

### 6. Auth0 JWT Libraries

**Replaces:** jose-jwt

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>${java-jwt.version}</version>
</dependency>

<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>jwks-rsa</artifactId>
    <version>${jwks-rsa.version}</version>
</dependency>
```

| Feature | jose-jwt (.NET) | java-jwt (Java) |
|---------|----------------|-----------------|
| Decode Token | `JWT.Decode(token, key)` | `JWT.decode(token)` |
| Verify Token | `JWT.Decode(token, key, JwsAlgorithm.RS256)` | `JWT.require(algorithm).build().verify(token)` |
| Get Claims | `JWT.Payload<T>(token)` | `decodedJWT.getClaims()` |
| JWKS Fetching | Manual | `JwkProviderBuilder` |

**Migration Notes:**
- Auth0's java-jwt provides similar API to jose-jwt
- jwks-rsa handles JWKS endpoint fetching with caching
- Used for manual token validation if needed

### 7. Springdoc OpenAPI

**Replaces:** Microsoft.AspNet.WebApi.HelpPage

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc.version}</version>
</dependency>
```

| Feature | ASP.NET HelpPage | Springdoc OpenAPI |
|---------|-----------------|-------------------|
| API Discovery | ApiExplorer | Automatic scanning |
| Documentation UI | Custom views | Swagger UI |
| Schema Generation | ModelDescriptionGenerator | Automatic |
| Try It Out | Not available | Built-in |
| OpenAPI Spec | Not available | `/v3/api-docs` |

**Migration Notes:**
- Springdoc provides more features than HelpPage
- Interactive Swagger UI for API testing
- OpenAPI 3.0 specification support

### 8. Spring Boot Validation

**Replaces:** System.ComponentModel.DataAnnotations

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

| .NET Annotation | Java Annotation |
|-----------------|-----------------|
| `[Required]` | `@NotNull` / `@NotBlank` |
| `[StringLength(max)]` | `@Size(max = max)` |
| `[Range(min, max)]` | `@Min(min)` / `@Max(max)` |
| `[EmailAddress]` | `@Email` |
| `[RegularExpression]` | `@Pattern` |

### 9. Spring Boot Actuator

**Replaces:** Custom health endpoints

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

| Feature | .NET | Spring Actuator |
|---------|------|-----------------|
| Health Check | Custom | `/actuator/health` |
| Info Endpoint | Custom | `/actuator/info` |
| Metrics | Custom | `/actuator/metrics` |

### 10. Development Tools

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

| Tool | Purpose |
|------|---------|
| DevTools | Hot reload during development |
| Lombok | Reduce boilerplate code |
| Configuration Processor | IDE support for properties |

### 11. Testing Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

| Feature | .NET | Spring Test |
|---------|------|-------------|
| Unit Testing | MSTest/NUnit | JUnit 5 |
| Mocking | Moq | Mockito |
| Web Testing | TestServer | MockMvc |
| Security Testing | Custom | `@WithMockUser` |

## Complete pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
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
        <springdoc.version>2.3.0</springdoc.version>
        <java-jwt.version>4.4.0</java-jwt.version>
        <jwks-rsa.version>0.22.1</jwks-rsa.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Boot OAuth2 Resource Server -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
        </dependency>

        <!-- Thymeleaf -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- Thymeleaf Spring Security integration -->
        <dependency>
            <groupId>org.thymeleaf.extras</groupId>
            <artifactId>thymeleaf-extras-springsecurity6</artifactId>
        </dependency>

        <!-- Jackson JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- Auth0 JWT -->
        <dependency>
            <groupId>com.auth0</groupId>
            <artifactId>java-jwt</artifactId>
            <version>${java-jwt.version}</version>
        </dependency>

        <!-- Auth0 JWKS RSA -->
        <dependency>
            <groupId>com.auth0</groupId>
            <artifactId>jwks-rsa</artifactId>
            <version>${jwks-rsa.version}</version>
        </dependency>

        <!-- Springdoc OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- DevTools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Configuration Processor -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Dependency Tree Summary

```
descope-sample-app
├── spring-boot-starter-web
│   ├── spring-boot-starter-tomcat
│   ├── spring-webmvc
│   └── jackson-databind
├── spring-boot-starter-security
│   └── spring-security-web
├── spring-boot-starter-oauth2-resource-server
│   ├── spring-security-oauth2-resource-server
│   └── nimbus-jose-jwt
├── spring-boot-starter-thymeleaf
│   └── thymeleaf-spring6
├── java-jwt (Auth0)
├── jwks-rsa (Auth0)
├── springdoc-openapi-starter-webmvc-ui
│   └── swagger-ui
├── spring-boot-starter-validation
│   └── hibernate-validator
├── spring-boot-starter-actuator
├── spring-boot-starter-test
│   ├── junit-jupiter
│   └── mockito
└── spring-security-test
```

## Verification

Dependencies were verified by:

1. **Compilation**: `mvn compile` completed successfully
2. **Dependency Resolution**: All dependencies resolved without conflicts
3. **Application Startup**: Application starts with all features functional

## Summary

Task B2 configured all required Maven dependencies for the Spring Boot migration. The dependency mapping ensures feature parity with the original .NET application while leveraging Spring Boot's ecosystem for enhanced functionality like automatic JWT validation and interactive API documentation.
