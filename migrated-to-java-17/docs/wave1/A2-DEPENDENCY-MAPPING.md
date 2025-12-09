# Task A2: .NET to Java Dependency Mapping

## Executive Summary

This document provides a comprehensive mapping of all .NET Framework 4.8 dependencies to their Java 17/Spring Boot 3.2 equivalents. Each mapping includes version recommendations, API differences, and migration notes.

## Dependency Mapping Overview

### Legend

| Symbol | Meaning |
|--------|---------|
| Direct | Direct 1:1 mapping available |
| Partial | Partial mapping, some features differ |
| Alternative | Different approach in Java |
| Built-in | Functionality built into Spring Boot |
| N/A | Not needed in Java/Spring Boot |

## Core Framework Mapping

### ASP.NET MVC & Web API → Spring Boot Web

| .NET Package | Version | Java Equivalent | Version | Mapping Type |
|-------------|---------|-----------------|---------|--------------|
| Microsoft.AspNet.Mvc | 5.2.9 | spring-boot-starter-web | 3.2.0 | Direct |
| Microsoft.AspNet.WebApi | 5.2.9 | spring-boot-starter-web | 3.2.0 | Direct |
| Microsoft.AspNet.WebApi.Core | 5.2.9 | spring-boot-starter-web | 3.2.0 | Direct |
| Microsoft.AspNet.WebApi.Client | 5.2.9 | spring-boot-starter-webflux (WebClient) | 3.2.0 | Alternative |
| Microsoft.AspNet.WebApi.WebHost | 5.2.9 | spring-boot-starter-web | 3.2.0 | Built-in |
| Microsoft.AspNet.Razor | 3.2.9 | spring-boot-starter-thymeleaf | 3.2.0 | Alternative |
| Microsoft.AspNet.WebPages | 3.2.9 | spring-boot-starter-thymeleaf | 3.2.0 | Alternative |

**Migration Notes:**
- Spring Boot combines MVC and REST API in a single `spring-boot-starter-web` dependency
- Razor views should be migrated to Thymeleaf templates
- Web API controllers become `@RestController` classes
- MVC controllers become `@Controller` classes

### Authentication & JWT

| .NET Package | Version | Java Equivalent | Version | Mapping Type |
|-------------|---------|-----------------|---------|--------------|
| jose-jwt | 5.0.0 | com.auth0:java-jwt | 4.4.0 | Direct |
| System.IdentityModel.Tokens.Jwt | 7.5.1 | spring-boot-starter-oauth2-resource-server | 3.2.0 | Alternative |
| Microsoft.IdentityModel.Tokens | 7.5.1 | spring-boot-starter-oauth2-resource-server | 3.2.0 | Built-in |
| Microsoft.IdentityModel.JsonWebTokens | 7.5.1 | com.auth0:java-jwt | 4.4.0 | Direct |
| Microsoft.IdentityModel.Logging | 7.5.1 | SLF4J/Logback (Spring Boot default) | 2.0.9 | Built-in |
| Microsoft.IdentityModel.Abstractions | 7.5.1 | N/A | - | N/A |

**Migration Notes:**
- Spring Security OAuth2 Resource Server provides built-in JWT validation
- For manual JWT handling, use Auth0's `java-jwt` library
- JWKS fetching is handled by `com.auth0:jwks-rsa` library
- Spring Security filter chain replaces manual token validation in controllers

### JSON Processing

| .NET Package | Version | Java Equivalent | Version | Mapping Type |
|-------------|---------|-----------------|---------|--------------|
| Newtonsoft.Json | 13.0.3 | com.fasterxml.jackson.core:jackson-databind | 2.15.3 | Direct |
| System.Text.Json | 4.7.2 | com.fasterxml.jackson.core:jackson-databind | 2.15.3 | Direct |

**Migration Notes:**
- Jackson is the default JSON library in Spring Boot
- Most Newtonsoft.Json features have Jackson equivalents
- Attribute mapping: `[JsonProperty]` → `@JsonProperty`
- Custom converters: `JsonConverter` → `JsonSerializer`/`JsonDeserializer`

### API Documentation

| .NET Package | Version | Java Equivalent | Version | Mapping Type |
|-------------|---------|-----------------|---------|--------------|
| Microsoft.AspNet.WebApi.HelpPage | 5.2.9 | org.springdoc:springdoc-openapi-starter-webmvc-ui | 2.3.0 | Alternative |

**Migration Notes:**
- ASP.NET HelpPage is replaced by Springdoc OpenAPI (Swagger)
- OpenAPI provides more features than HelpPage
- Annotations: XML comments → `@Operation`, `@ApiResponse`, `@Schema`
- Auto-discovery works similarly in both frameworks

### Frontend Assets

| .NET Package | Version | Java Equivalent | Version | Mapping Type |
|-------------|---------|-----------------|---------|--------------|
| Bootstrap | 5.2.3 | CDN or WebJars | 5.2.3 | Direct |
| jQuery | 3.4.1 | CDN or WebJars | 3.4.1 | Direct |
| Modernizr | 2.8.3 | CDN or WebJars | 2.8.3 | Direct |

**Migration Notes:**
- Recommend using CDN links for simplicity
- WebJars can be used for offline/bundled deployment
- No bundling equivalent needed (use build tools like Webpack if required)

### Asset Bundling & Optimization

| .NET Package | Version | Java Equivalent | Version | Mapping Type |
|-------------|---------|-----------------|---------|--------------|
| Microsoft.AspNet.Web.Optimization | 1.1.3 | N/A (use CDN or build tools) | - | Alternative |
| WebGrease | 1.6.0 | N/A | - | N/A |
| Antlr | 3.5.0.2 | N/A | - | N/A |

**Migration Notes:**
- Spring Boot doesn't have built-in bundling
- Use CDN for production assets
- For complex bundling needs, use Webpack, Vite, or similar build tools
- Thymeleaf can inline resources if needed

### Infrastructure & Runtime

| .NET Package | Version | Java Equivalent | Version | Mapping Type |
|-------------|---------|-----------------|---------|--------------|
| Microsoft.Web.Infrastructure | 2.0.1 | N/A | - | Built-in |
| Microsoft.CodeDom.Providers.DotNetCompilerPlatform | 2.0.1 | N/A | - | N/A |
| Microsoft.Bcl.AsyncInterfaces | 1.1.0 | java.util.concurrent | Built-in | Built-in |
| System.Buffers | 4.5.1 | java.nio | Built-in | Built-in |
| System.Memory | 4.5.5 | java.nio | Built-in | Built-in |
| System.Numerics.Vectors | 4.5.0 | jdk.incubator.vector (Java 16+) | Built-in | Built-in |
| System.Runtime.CompilerServices.Unsafe | 6.0.0 | sun.misc.Unsafe (discouraged) | Built-in | N/A |
| System.Text.Encodings.Web | 4.7.2 | java.net.URLEncoder | Built-in | Built-in |
| System.Threading.Tasks.Extensions | 4.5.4 | java.util.concurrent.CompletableFuture | Built-in | Built-in |
| System.ValueTuple | 4.5.0 | Java Records (Java 16+) | Built-in | Built-in |

**Migration Notes:**
- Most .NET infrastructure packages have Java built-in equivalents
- Async/await patterns map to CompletableFuture or reactive streams
- ValueTuple can be replaced with Java Records

## Detailed API Mapping

### Controller Annotations

| .NET | Java/Spring |
|------|-------------|
| `[Route("api/[controller]")]` | `@RequestMapping("/api/controller")` |
| `[HttpGet]` | `@GetMapping` |
| `[HttpPost]` | `@PostMapping` |
| `[HttpPut]` | `@PutMapping` |
| `[HttpDelete]` | `@DeleteMapping` |
| `[HttpPatch]` | `@PatchMapping` |
| `[FromBody]` | `@RequestBody` |
| `[FromQuery]` | `@RequestParam` |
| `[FromRoute]` | `@PathVariable` |
| `[FromHeader]` | `@RequestHeader` |
| `[Authorize]` | `@PreAuthorize` or Security config |

### Return Types

| .NET | Java/Spring |
|------|-------------|
| `IHttpActionResult` | `ResponseEntity<T>` |
| `Ok(data)` | `ResponseEntity.ok(data)` |
| `BadRequest()` | `ResponseEntity.badRequest().build()` |
| `NotFound()` | `ResponseEntity.notFound().build()` |
| `Unauthorized()` | `ResponseEntity.status(401).build()` |
| `Created(uri, data)` | `ResponseEntity.created(uri).body(data)` |
| `NoContent()` | `ResponseEntity.noContent().build()` |
| `ActionResult` | `String` (view name) |
| `View()` | `return "viewName"` |
| `RedirectToAction()` | `return "redirect:/path"` |

### Model Binding & Validation

| .NET | Java/Spring |
|------|-------------|
| `[Required]` | `@NotNull` / `@NotBlank` |
| `[StringLength(max)]` | `@Size(max = max)` |
| `[Range(min, max)]` | `@Min(min)` / `@Max(max)` |
| `[EmailAddress]` | `@Email` |
| `[RegularExpression]` | `@Pattern` |
| `ModelState.IsValid` | `BindingResult.hasErrors()` |
| `[ValidateAntiForgeryToken]` | CSRF protection in SecurityConfig |

### Configuration

| .NET (Web.config) | Java (application.properties) |
|-------------------|-------------------------------|
| `<appSettings>` | `custom.property=value` |
| `<connectionStrings>` | `spring.datasource.*` |
| `<system.web>` | Various Spring properties |
| `ConfigurationManager.AppSettings["key"]` | `@Value("${key}")` |

### Dependency Injection

| .NET | Java/Spring |
|------|-------------|
| `services.AddScoped<T>()` | `@Scope("request")` on `@Component` |
| `services.AddSingleton<T>()` | `@Component` (default singleton) |
| `services.AddTransient<T>()` | `@Scope("prototype")` on `@Component` |
| Constructor injection | Constructor injection (same) |
| `[FromServices]` | `@Autowired` (field) or constructor |

### JWT/Authentication

| .NET (jose-jwt) | Java (java-jwt) |
|-----------------|-----------------|
| `JWT.Decode(token, key)` | `JWT.require(algorithm).build().verify(token)` |
| `JWT.Payload<T>(token)` | `JWT.decode(token).getClaims()` |
| `JwkSet.FromJson(json)` | `JwkProvider.get(keyId)` |
| `SecurityTokenValidationException` | `JWTVerificationException` |

## Maven Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Web (replaces ASP.NET MVC + Web API) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Security (replaces manual auth) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- OAuth2 Resource Server (replaces System.IdentityModel.Tokens.Jwt) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- Thymeleaf (replaces Razor) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- JWT Library (replaces jose-jwt) -->
    <dependency>
        <groupId>com.auth0</groupId>
        <artifactId>java-jwt</artifactId>
        <version>4.4.0</version>
    </dependency>

    <!-- JWKS RSA (for fetching public keys) -->
    <dependency>
        <groupId>com.auth0</groupId>
        <artifactId>jwks-rsa</artifactId>
        <version>0.22.1</version>
    </dependency>

    <!-- OpenAPI/Swagger (replaces HelpPage) -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- Validation (replaces DataAnnotations) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Jackson is included in spring-boot-starter-web -->
    <!-- No explicit dependency needed for JSON processing -->
</dependencies>
```

## Version Compatibility Matrix

| Component | .NET Version | Java Version | Notes |
|-----------|--------------|--------------|-------|
| Framework | .NET 4.8 | Java 17 | LTS versions |
| Web Framework | ASP.NET MVC 5.2.9 | Spring Boot 3.2.0 | Latest stable |
| JSON Library | Newtonsoft.Json 13.0.3 | Jackson 2.15.x | Spring Boot managed |
| JWT Library | jose-jwt 5.0.0 | java-jwt 4.4.0 | Auth0 library |
| API Docs | HelpPage 5.2.9 | Springdoc 2.3.0 | OpenAPI 3.0 |
| Template Engine | Razor 3.2.9 | Thymeleaf 3.1.x | Spring Boot managed |

## Migration Complexity Assessment

| Category | Complexity | Effort | Notes |
|----------|------------|--------|-------|
| Controllers | Low | 2-4 hours | Annotation-based, similar patterns |
| Authentication | Medium | 4-8 hours | Different approach in Spring Security |
| Views/Templates | Medium | 4-8 hours | Syntax differences between Razor and Thymeleaf |
| Configuration | Low | 1-2 hours | Properties file vs XML |
| API Documentation | Low | 2-4 hours | Springdoc is more feature-rich |
| JWT Validation | Medium | 4-6 hours | Different libraries, similar concepts |
| Static Assets | Low | 1-2 hours | CDN or WebJars |

**Total Estimated Effort:** 18-34 hours

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| JWT validation differences | High | Comprehensive testing with real Descope tokens |
| Thymeleaf syntax learning curve | Medium | Use migration guide, test each template |
| Spring Security complexity | Medium | Follow Spring Security best practices |
| Missing Newtonsoft.Json features | Low | Jackson covers most use cases |
| HelpPage to OpenAPI differences | Low | OpenAPI is more capable |

## Summary

The .NET to Java migration is straightforward with well-established equivalents for all major dependencies. The primary areas requiring attention are:

1. **Authentication:** Spring Security's declarative approach differs from manual token validation
2. **Templates:** Razor to Thymeleaf requires syntax conversion
3. **Configuration:** XML to properties file format change

All other dependencies have direct or near-direct equivalents in the Java ecosystem.
