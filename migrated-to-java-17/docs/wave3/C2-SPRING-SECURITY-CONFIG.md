# Task C2: Spring Security Configuration

## Executive Summary

This document details the Spring Security configuration that replaces the manual authentication handling from the .NET Framework 4.8 application. The configuration establishes JWT-based authentication using Spring Security's OAuth2 Resource Server.

## Source Analysis

### .NET Authentication Approach

In the .NET application, authentication was handled manually in each controller:

**SampleController.cs:**

```csharp
public class SampleController : ApiController
{
    public async Task<IHttpActionResult> Get()
    {
        var authorizationHeader = Request.Headers.Authorization;
        if (authorizationHeader != null && 
            authorizationHeader.Scheme.Equals("Bearer", StringComparison.OrdinalIgnoreCase))
        {
            var sessionToken = authorizationHeader.Parameter;
            if (!string.IsNullOrEmpty(sessionToken))
            { 
                var tokenValidator = new TokenValidator("P2dI0leWLEC45BDmfxeOCSSOWiCt");
                try
                {
                    var claimsPrincipal = await tokenValidator.ValidateSession(sessionToken);
                    return Ok("This is a sample API endpoint.");
                }
                catch (SecurityTokenValidationException ex)
                {
                    return Unauthorized();
                }
            }
        }
        return Unauthorized();
    }
}
```

### Key Characteristics of .NET Approach

| Aspect | Implementation |
|--------|----------------|
| Authentication Location | Inside each controller action |
| Token Extraction | Manual from `Request.Headers.Authorization` |
| Token Validation | Manual call to `TokenValidator.ValidateSession()` |
| Error Handling | Try-catch with `Unauthorized()` response |
| Reusability | Code duplicated in each protected endpoint |

## Java Implementation

### SecurityConfig.java

**Location:** `src/main/java/com/descope/sample/config/SecurityConfig.java`

```java
package com.descope.sample.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${descope.project-id}")
    private String descopeProjectId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/login", "/login.html", "/authenticated.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/static/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Protected API endpoints require JWT authentication
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri("https://api.descope.com/" + descopeProjectId + "/.well-known/jwks.json")
                )
            );

        return http.build();
    }
}
```

## Configuration Breakdown

### 1. Class Annotations

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
```

| Annotation | Purpose |
|------------|---------|
| `@Configuration` | Marks class as Spring configuration |
| `@EnableWebSecurity` | Enables Spring Security web security |

### 2. Project ID Injection

```java
@Value("${descope.project-id}")
private String descopeProjectId;
```

- Injects Descope project ID from `application.properties`
- Supports environment variable override via `${DESCOPE_PROJECT_ID}`

### 3. CSRF Configuration

```java
.csrf(csrf -> csrf.disable())
```

| .NET | Spring Boot |
|------|-------------|
| `[ValidateAntiForgeryToken]` attribute | `csrf.disable()` for stateless APIs |
| Enabled by default for forms | Disabled for JWT-based authentication |

**Rationale:** CSRF protection is not needed for stateless JWT authentication because:
- No cookies are used for authentication
- Each request includes the JWT in the Authorization header
- The token itself provides request authenticity

### 4. Session Management

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

| Policy | Description |
|--------|-------------|
| `STATELESS` | No HTTP session created or used |
| `IF_REQUIRED` | Session created only if needed (default) |
| `ALWAYS` | Session always created |
| `NEVER` | Never create session, but use if exists |

**Rationale:** JWT authentication is stateless - all authentication information is contained in the token.

### 5. Endpoint Authorization

```java
.authorizeHttpRequests(auth -> auth
    // Public endpoints
    .requestMatchers("/", "/login", "/login.html", "/authenticated.html").permitAll()
    .requestMatchers("/css/**", "/js/**", "/static/**").permitAll()
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
    .requestMatchers("/actuator/health").permitAll()
    // Protected API endpoints require JWT authentication
    .requestMatchers("/api/**").authenticated()
    .anyRequest().permitAll()
)
```

#### Endpoint Categories

| Category | Endpoints | Access |
|----------|-----------|--------|
| Views | `/`, `/login`, `/login.html`, `/authenticated.html` | Public |
| Static Resources | `/css/**`, `/js/**`, `/static/**` | Public |
| API Documentation | `/swagger-ui/**`, `/v3/api-docs/**` | Public |
| Health Check | `/actuator/health` | Public |
| REST API | `/api/**` | Authenticated |
| Other | `anyRequest()` | Public |

#### Comparison with .NET

| .NET | Spring Boot |
|------|-------------|
| Manual check in controller | Declarative configuration |
| `[Authorize]` attribute | `.authenticated()` |
| `[AllowAnonymous]` attribute | `.permitAll()` |
| Route-based in RouteConfig | Pattern-based matchers |

### 6. OAuth2 Resource Server Configuration

```java
.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt
        .jwkSetUri("https://api.descope.com/" + descopeProjectId + "/.well-known/jwks.json")
    )
)
```

| Configuration | Purpose |
|---------------|---------|
| `oauth2ResourceServer()` | Enables OAuth2 resource server |
| `.jwt()` | Configures JWT-based authentication |
| `.jwkSetUri()` | Sets JWKS endpoint for public key retrieval |

## Authentication Flow Comparison

### .NET Manual Authentication

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        .NET Authentication Flow                              │
└─────────────────────────────────────────────────────────────────────────────┘

Request with Bearer Token
        │
        ▼
┌───────────────────────────────────────┐
│ Controller Action                      │
│ - Check Authorization header           │
│ - Extract Bearer token                 │
│ - Create TokenValidator instance       │
│ - Call ValidateSession()               │
│ - Handle exceptions                    │
└───────────────────────────────────────┘
        │
        ├─── Valid Token ──▶ Return Ok()
        │
        └─── Invalid Token ──▶ Return Unauthorized()
```

### Spring Security Authentication

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     Spring Security Authentication Flow                      │
└─────────────────────────────────────────────────────────────────────────────┘

Request with Bearer Token
        │
        ▼
┌───────────────────────────────────────┐
│ Security Filter Chain                  │
│ 1. DisableEncodeUrlFilter             │
│ 2. WebAsyncManagerIntegrationFilter   │
│ 3. SecurityContextHolderFilter        │
│ 4. HeaderWriterFilter                 │
│ 5. CorsFilter                         │
│ 6. LogoutFilter                       │
│ 7. BearerTokenAuthenticationFilter ◀──┼── JWT Validation Here
│ 8. RequestCacheAwareFilter            │
│ 9. SecurityContextHolderAwareFilter   │
│ 10. AnonymousAuthenticationFilter     │
│ 11. SessionManagementFilter           │
│ 12. ExceptionTranslationFilter        │
│ 13. AuthorizationFilter               │
└───────────────────────────────────────┘
        │
        ├─── Valid Token ──▶ Controller (with SecurityContext populated)
        │
        └─── Invalid Token ──▶ 401 Unauthorized (automatic)
```

## JWT Validation Process

### Spring Security JWT Validation Steps

1. **Token Extraction**
   - `BearerTokenAuthenticationFilter` extracts token from `Authorization: Bearer <token>` header

2. **Token Decoding**
   - `NimbusJwtDecoder` decodes the JWT
   - Extracts header, payload, and signature

3. **Key Retrieval**
   - Fetches JWKS from configured URI
   - Caches keys for performance
   - Selects key by `kid` (key ID) claim

4. **Signature Verification**
   - Verifies JWT signature using public key
   - Supports RS256, RS384, RS512 algorithms

5. **Claims Validation**
   - Validates `exp` (expiration) claim
   - Validates `iat` (issued at) claim
   - Validates `nbf` (not before) claim if present

6. **Security Context Population**
   - Creates `JwtAuthenticationToken`
   - Populates `SecurityContextHolder`
   - Makes JWT available via `@AuthenticationPrincipal`

## Controller Integration

### .NET Controller (Manual Validation)

```csharp
public async Task<IHttpActionResult> Get()
{
    var authorizationHeader = Request.Headers.Authorization;
    if (authorizationHeader != null && 
        authorizationHeader.Scheme.Equals("Bearer", StringComparison.OrdinalIgnoreCase))
    {
        var sessionToken = authorizationHeader.Parameter;
        var tokenValidator = new TokenValidator("...");
        try
        {
            var claimsPrincipal = await tokenValidator.ValidateSession(sessionToken);
            return Ok("This is a sample API endpoint.");
        }
        catch (SecurityTokenValidationException ex)
        {
            return Unauthorized();
        }
    }
    return Unauthorized();
}
```

### Spring Controller (Automatic Validation)

```java
@GetMapping("/api/sample")
public ResponseEntity<String> get(@AuthenticationPrincipal Jwt jwt) {
    // JWT is automatically validated by Spring Security
    // Access claims if needed:
    // String subject = jwt.getSubject();
    // Map<String, Object> claims = jwt.getClaims();
    return ResponseEntity.ok("This is a sample API endpoint.");
}
```

## Benefits of Spring Security Approach

| Benefit | Description |
|---------|-------------|
| **Declarative** | Security rules defined in configuration, not code |
| **Centralized** | All security logic in one place |
| **Reusable** | No code duplication across controllers |
| **Testable** | Easy to test with `@WithMockUser` |
| **Extensible** | Easy to add custom filters or validators |
| **Standard** | Follows OAuth2/JWT best practices |

## Error Handling

### Automatic Error Responses

| Scenario | HTTP Status | Response |
|----------|-------------|----------|
| Missing token | 401 | `WWW-Authenticate: Bearer` |
| Invalid token | 401 | `WWW-Authenticate: Bearer error="invalid_token"` |
| Expired token | 401 | `WWW-Authenticate: Bearer error="invalid_token", error_description="..."` |
| Insufficient scope | 403 | `WWW-Authenticate: Bearer error="insufficient_scope"` |

### Custom Error Handling (Optional)

```java
.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt.jwkSetUri(getJwksUri()))
    .authenticationEntryPoint((request, response, exception) -> {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Unauthorized\"}");
    })
)
```

## Verification

The SecurityConfig implementation was verified by:

1. **Compilation**: `mvn compile` completed successfully
2. **Filter Chain**: Security filter chain logs show correct configuration
3. **Public Endpoints**: Accessible without authentication
4. **Protected Endpoints**: Return 401 without valid JWT
5. **JWT Validation**: Valid Descope tokens are accepted

## Summary

Task C2 successfully configured Spring Security to replace the manual authentication handling from the .NET application. The declarative approach provides centralized security configuration, automatic JWT validation, and follows OAuth2/JWT best practices. Controllers no longer need to handle authentication logic, making the code cleaner and more maintainable.
