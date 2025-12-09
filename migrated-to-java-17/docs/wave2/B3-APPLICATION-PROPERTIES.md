# Task B3: Application Properties Configuration

## Executive Summary

This document details the Spring Boot application.properties configuration that replaces the Web.config from the .NET Framework 4.8 application. Each configuration section is mapped from its .NET equivalent with explanations and migration notes.

## Configuration Overview

### Source (.NET): Web.config

The .NET application used XML-based configuration in `Web.config`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <appSettings>
    <add key="webpages:Version" value="3.0.0.0" />
    <add key="webpages:Enabled" value="false" />
    <add key="ClientValidationEnabled" value="true" />
    <add key="UnobtrusiveJavaScriptEnabled" value="true" />
  </appSettings>
  <system.web>
    <compilation debug="true" targetFramework="4.8" />
    <httpRuntime targetFramework="4.8" />
  </system.web>
  <!-- Additional configuration sections -->
</configuration>
```

### Target (Spring Boot): application.properties

Spring Boot uses properties-based configuration:

```properties
# Server Configuration
server.port=8080

# Descope Configuration
descope.project-id=${DESCOPE_PROJECT_ID:YOUR_DESCOPE_PROJECT_ID}

# Additional configuration sections
```

## Complete Configuration Mapping

### 1. Application Identity

**Source (.NET):** Assembly attributes and Web.config

**Target (Spring Boot):**

```properties
# Application name
spring.application.name=descope-sample-app
```

| .NET | Spring Boot |
|------|-------------|
| AssemblyInfo.cs | `spring.application.name` |
| Web.config appSettings | `application.properties` |

### 2. Server Configuration

**Source (.NET):** IIS configuration

**Target (Spring Boot):**

```properties
# Server configuration
server.port=8080
```

| .NET (IIS) | Spring Boot |
|------------|-------------|
| IIS binding port | `server.port` |
| IIS application pool | Embedded Tomcat |
| IIS virtual directory | Context path |

**Migration Notes:**
- Spring Boot uses embedded Tomcat by default
- Port configuration is straightforward
- No external web server configuration needed

### 3. Descope Integration

**Source (.NET):** Environment variable in TokenValidator.cs

```csharp
public static string DescopeProjectId => 
    Environment.GetEnvironmentVariable("DESCOPE_PROJECT_ID") 
    ?? "P2dI0leWLEC45BDmfxeOCSSOWiCt";
```

**Target (Spring Boot):**

```properties
# =============================================================================
# Descope Configuration
# =============================================================================
# Set your Descope Project ID here or via environment variable DESCOPE_PROJECT_ID
descope.project-id=${DESCOPE_PROJECT_ID:YOUR_DESCOPE_PROJECT_ID}
```

| .NET | Spring Boot |
|------|-------------|
| `Environment.GetEnvironmentVariable()` | `${ENV_VAR:default}` syntax |
| Hardcoded fallback | Property default value |

**Migration Notes:**
- Spring Boot supports environment variable interpolation
- Default value syntax: `${VAR_NAME:default_value}`
- Can be overridden via command line: `--descope.project-id=xxx`

### 4. JWT/OAuth2 Configuration

**Source (.NET):** Manual JWKS URL construction in TokenValidator.cs

```csharp
private async Task<JwkSet> GetPublicKeyAsync(string projectId)
{
    var jwksUrl = $"https://api.descope.com/{projectId}/.well-known/jwks.json";
    // ...
}
```

**Target (Spring Boot):**

```properties
# =============================================================================
# Spring Security OAuth2 Resource Server (JWT Validation)
# =============================================================================
# This replaces the TokenValidator.cs JWT validation logic
# The JWKS URI is constructed from the Descope project ID
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://api.descope.com/${descope.project-id}/.well-known/jwks.json
```

| .NET | Spring Boot |
|------|-------------|
| Manual HttpClient call | Automatic JWKS fetching |
| Manual JWT validation | Spring Security filter |
| Custom TokenValidator | OAuth2 Resource Server |

**Migration Notes:**
- Spring Security automatically fetches and caches JWKS
- JWT validation happens in the security filter chain
- No manual token validation code needed in controllers

### 5. Thymeleaf Configuration

**Source (.NET):** Razor configuration in Web.config

```xml
<appSettings>
    <add key="webpages:Version" value="3.0.0.0" />
    <add key="webpages:Enabled" value="false" />
</appSettings>
```

**Target (Spring Boot):**

```properties
# =============================================================================
# Thymeleaf Configuration (replaces Razor views)
# =============================================================================
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false
```

| Property | Purpose | Default |
|----------|---------|---------|
| `prefix` | Template location | `classpath:/templates/` |
| `suffix` | File extension | `.html` |
| `mode` | Template mode | `HTML` |
| `encoding` | Character encoding | `UTF-8` |
| `cache` | Template caching | `true` (disabled for dev) |

**Migration Notes:**
- Templates located in `src/main/resources/templates/`
- Caching disabled for development (enable in production)
- HTML mode for natural templates

### 6. OpenAPI/Swagger Configuration

**Source (.NET):** HelpPage area configuration

```csharp
// HelpPageConfig.cs
public static void Register(HttpConfiguration config)
{
    config.SetDocumentationProvider(new XmlDocumentationProvider(...));
}
```

**Target (Spring Boot):**

```properties
# =============================================================================
# OpenAPI/Swagger Configuration (replaces ASP.NET Help Page)
# =============================================================================
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
```

| Property | Purpose |
|----------|---------|
| `api-docs.path` | OpenAPI JSON endpoint |
| `swagger-ui.path` | Swagger UI URL |
| `swagger-ui.enabled` | Enable/disable UI |
| `operationsSorter` | Sort operations by method |
| `tagsSorter` | Sort tags alphabetically |

**Endpoint Mapping:**

| .NET HelpPage | Spring Boot Springdoc |
|---------------|----------------------|
| `/Help` | `/swagger-ui.html` |
| `/Help/Api/{id}` | Swagger UI (interactive) |
| N/A | `/v3/api-docs` (JSON) |

### 7. Actuator Configuration

**Source (.NET):** Custom health endpoints (if any)

**Target (Spring Boot):**

```properties
# =============================================================================
# Actuator Configuration (health checks and monitoring)
# =============================================================================
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
```

| Property | Purpose |
|----------|---------|
| `exposure.include` | Exposed endpoints |
| `show-details` | Health detail visibility |

**Available Endpoints:**

| Endpoint | URL | Purpose |
|----------|-----|---------|
| Health | `/actuator/health` | Application health status |
| Info | `/actuator/info` | Application information |

### 8. Logging Configuration

**Source (.NET):** Web.config system.diagnostics or custom logging

**Target (Spring Boot):**

```properties
# =============================================================================
# Logging Configuration
# =============================================================================
logging.level.root=INFO
logging.level.com.descope.sample=DEBUG
logging.level.org.springframework.security=DEBUG
```

| Property | Purpose |
|----------|---------|
| `logging.level.root` | Default log level |
| `logging.level.com.descope.sample` | Application log level |
| `logging.level.org.springframework.security` | Security debug logging |

**Log Levels:**

| Level | Description |
|-------|-------------|
| TRACE | Most detailed |
| DEBUG | Debug information |
| INFO | General information |
| WARN | Warnings |
| ERROR | Errors only |

### 9. Jackson JSON Configuration

**Source (.NET):** Newtonsoft.Json settings

```csharp
// Global.asax.cs or WebApiConfig.cs
config.Formatters.JsonFormatter.SerializerSettings.NullValueHandling = NullValueHandling.Ignore;
config.Formatters.JsonFormatter.SerializerSettings.Formatting = Formatting.Indented;
```

**Target (Spring Boot):**

```properties
# =============================================================================
# Jackson JSON Configuration (replaces Newtonsoft.Json settings)
# =============================================================================
spring.jackson.serialization.indent-output=true
spring.jackson.default-property-inclusion=non-null
```

| .NET (Newtonsoft.Json) | Spring Boot (Jackson) |
|------------------------|----------------------|
| `Formatting.Indented` | `indent-output=true` |
| `NullValueHandling.Ignore` | `default-property-inclusion=non-null` |

## Complete application.properties

```properties
# =============================================================================
# Descope Sample Application - Spring Boot Configuration
# =============================================================================
# This file replaces Web.config from the .NET application.
# 
# Migrated from: DescopeSampleApp/Web.config
# =============================================================================

# Application name
spring.application.name=descope-sample-app

# Server configuration
server.port=8080

# =============================================================================
# Descope Configuration
# =============================================================================
# Set your Descope Project ID here or via environment variable DESCOPE_PROJECT_ID
descope.project-id=${DESCOPE_PROJECT_ID:YOUR_DESCOPE_PROJECT_ID}

# =============================================================================
# Spring Security OAuth2 Resource Server (JWT Validation)
# =============================================================================
# This replaces the TokenValidator.cs JWT validation logic
# The JWKS URI is constructed from the Descope project ID
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://api.descope.com/${descope.project-id}/.well-known/jwks.json

# =============================================================================
# Thymeleaf Configuration (replaces Razor views)
# =============================================================================
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.cache=false

# =============================================================================
# OpenAPI/Swagger Configuration (replaces ASP.NET Help Page)
# =============================================================================
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha

# =============================================================================
# Actuator Configuration (health checks and monitoring)
# =============================================================================
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized

# =============================================================================
# Logging Configuration
# =============================================================================
logging.level.root=INFO
logging.level.com.descope.sample=DEBUG
logging.level.org.springframework.security=DEBUG

# =============================================================================
# Jackson JSON Configuration (replaces Newtonsoft.Json settings)
# =============================================================================
spring.jackson.serialization.indent-output=true
spring.jackson.default-property-inclusion=non-null
```

## Environment Variable Support

Spring Boot supports multiple ways to override configuration:

### 1. Environment Variables

```bash
export DESCOPE_PROJECT_ID=your-project-id
java -jar descope-sample-app.jar
```

### 2. Command Line Arguments

```bash
java -jar descope-sample-app.jar --descope.project-id=your-project-id
```

### 3. External Properties File

```bash
java -jar descope-sample-app.jar --spring.config.location=file:/path/to/application.properties
```

### 4. Profile-Specific Configuration

Create `application-prod.properties` for production:

```properties
# Production configuration
spring.thymeleaf.cache=true
logging.level.root=WARN
logging.level.com.descope.sample=INFO
logging.level.org.springframework.security=WARN
```

Activate with:

```bash
java -jar descope-sample-app.jar --spring.profiles.active=prod
```

## Configuration Precedence

Spring Boot configuration precedence (highest to lowest):

1. Command line arguments
2. Environment variables
3. `application-{profile}.properties`
4. `application.properties`
5. Default values in code

## Verification

Configuration was verified by:

1. **Application Startup**: Application starts successfully with all configurations
2. **Endpoint Access**: All configured endpoints accessible
3. **JWT Validation**: JWKS URI correctly constructed
4. **Swagger UI**: Available at `/swagger-ui.html`
5. **Actuator**: Health endpoint at `/actuator/health`

## Summary

Task B3 established the complete application.properties configuration for the Spring Boot migration. The configuration provides feature parity with the original Web.config while leveraging Spring Boot's flexible configuration system. Environment variable support ensures secure handling of sensitive values like the Descope project ID.
