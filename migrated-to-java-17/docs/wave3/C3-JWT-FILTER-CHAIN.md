# Task C3: JWT Filter Chain Setup

## Executive Summary

This document details the integration of JWT validation into Spring Security's filter chain. Task C3 builds upon the TokenValidator (C1) and SecurityConfig (C2) to ensure complete JWT authentication flow for the migrated application.

## Prerequisites

- Task C1: TokenValidator implementation completed
- Task C2: Spring Security configuration completed

## Filter Chain Overview

### Spring Security Filter Chain

When a request arrives, it passes through a series of security filters before reaching the controller:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     Spring Security Filter Chain                             │
└─────────────────────────────────────────────────────────────────────────────┘

HTTP Request
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. DisableEncodeUrlFilter                                                    │
│    - Prevents session ID from being encoded in URLs                         │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. WebAsyncManagerIntegrationFilter                                          │
│    - Integrates SecurityContext with async request processing               │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. SecurityContextHolderFilter                                               │
│    - Manages SecurityContext lifecycle                                       │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. HeaderWriterFilter                                                        │
│    - Adds security headers (X-Content-Type-Options, X-Frame-Options, etc.) │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 5. CorsFilter                                                                │
│    - Handles Cross-Origin Resource Sharing                                   │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 6. LogoutFilter                                                              │
│    - Processes logout requests                                               │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 7. BearerTokenAuthenticationFilter  ◀── JWT VALIDATION HAPPENS HERE         │
│    - Extracts Bearer token from Authorization header                         │
│    - Delegates to JwtDecoder for validation                                  │
│    - Populates SecurityContext on success                                    │
│    - Returns 401 on failure                                                  │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 8. RequestCacheAwareFilter                                                   │
│    - Restores saved requests after authentication                           │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 9. SecurityContextHolderAwareRequestFilter                                   │
│    - Wraps request with security methods                                     │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 10. AnonymousAuthenticationFilter                                            │
│     - Creates anonymous authentication if none exists                        │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 11. SessionManagementFilter                                                  │
│     - Manages session creation policy (STATELESS in our case)               │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 12. ExceptionTranslationFilter                                               │
│     - Translates security exceptions to HTTP responses                       │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 13. AuthorizationFilter                                                      │
│     - Enforces authorization rules from SecurityConfig                       │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
Controller
```

## JWT Validation Flow

### BearerTokenAuthenticationFilter Details

The `BearerTokenAuthenticationFilter` is the key component for JWT authentication:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    BearerTokenAuthenticationFilter Flow                      │
└─────────────────────────────────────────────────────────────────────────────┘

Request with Authorization Header
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. Extract Token                                                             │
│    - Check for "Authorization: Bearer <token>" header                        │
│    - Extract token string                                                    │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ├─── No Token ──▶ Continue to next filter (anonymous access)
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. Create Authentication Request                                             │
│    - Create BearerTokenAuthenticationToken                                   │
│    - Pass to AuthenticationManager                                           │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. JWT Decoding (NimbusJwtDecoder)                                          │
│    - Parse JWT header, payload, signature                                    │
│    - Extract key ID (kid) from header                                        │
│    - Fetch public key from JWKS endpoint                                     │
│    - Verify signature                                                        │
│    - Validate claims (exp, iat, nbf)                                        │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ├─── Invalid Token ──▶ AuthenticationException ──▶ 401 Response
     │
     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. Create Authentication                                                     │
│    - Create JwtAuthenticationToken                                           │
│    - Extract authorities from claims                                         │
│    - Store in SecurityContextHolder                                          │
└─────────────────────────────────────────────────────────────────────────────┘
     │
     ▼
Continue to Controller
```

## Configuration Integration

### SecurityConfig.java (Complete)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${descope.project-id}")
    private String descopeProjectId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless JWT authentication
            .csrf(csrf -> csrf.disable())
            
            // Configure stateless session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure endpoint authorization
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
            
            // Configure OAuth2 Resource Server with JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri("https://api.descope.com/" + descopeProjectId + "/.well-known/jwks.json")
                )
            );

        return http.build();
    }
}
```

### Key Configuration Elements

| Element | Purpose | Impact on Filter Chain |
|---------|---------|------------------------|
| `csrf.disable()` | Disable CSRF protection | Removes CsrfFilter |
| `sessionCreationPolicy(STATELESS)` | No session creation | SessionManagementFilter configured for stateless |
| `authorizeHttpRequests()` | Define access rules | Configures AuthorizationFilter |
| `oauth2ResourceServer().jwt()` | Enable JWT authentication | Adds BearerTokenAuthenticationFilter |
| `jwkSetUri()` | JWKS endpoint | Configures NimbusJwtDecoder |

## JWKS Integration

### Descope JWKS Endpoint

```
https://api.descope.com/{projectId}/.well-known/jwks.json
```

### JWKS Response Format

```json
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "key-id-1",
      "use": "sig",
      "alg": "RS256",
      "n": "...",
      "e": "AQAB"
    }
  ]
}
```

### Key Caching

Spring Security's `NimbusJwtDecoder` automatically caches JWKS:

| Feature | Behavior |
|---------|----------|
| Initial Fetch | On first JWT validation |
| Cache Duration | Configurable (default varies) |
| Refresh | On cache miss or key rotation |
| Key Selection | By `kid` claim in JWT header |

## Error Handling

### Authentication Errors

| Error Type | HTTP Status | WWW-Authenticate Header |
|------------|-------------|-------------------------|
| Missing token | 401 | `Bearer` |
| Malformed token | 401 | `Bearer error="invalid_token"` |
| Expired token | 401 | `Bearer error="invalid_token", error_description="Jwt expired"` |
| Invalid signature | 401 | `Bearer error="invalid_token", error_description="..."` |
| Unknown key ID | 401 | `Bearer error="invalid_token"` |

### Authorization Errors

| Error Type | HTTP Status | Response |
|------------|-------------|----------|
| Insufficient permissions | 403 | Forbidden |
| Missing required scope | 403 | `Bearer error="insufficient_scope"` |

## Logging Configuration

### Enable Security Debug Logging

In `application.properties`:

```properties
logging.level.org.springframework.security=DEBUG
```

### Sample Log Output

```
DEBUG o.s.s.w.a.i.FilterSecurityInterceptor - Authorized filter invocation [GET /api/sample]
DEBUG o.s.s.o.s.r.a.JwtAuthenticationProvider - Authenticated token
DEBUG o.s.s.w.c.SecurityContextPersistenceFilter - Cleared SecurityContextHolder
```

## Testing the Filter Chain

### Test Public Endpoint

```bash
curl http://localhost:8080/
# Expected: 200 OK (no authentication required)
```

### Test Protected Endpoint Without Token

```bash
curl http://localhost:8080/api/sample
# Expected: 401 Unauthorized
# WWW-Authenticate: Bearer
```

### Test Protected Endpoint With Invalid Token

```bash
curl -H "Authorization: Bearer invalid-token" http://localhost:8080/api/sample
# Expected: 401 Unauthorized
# WWW-Authenticate: Bearer error="invalid_token"
```

### Test Protected Endpoint With Valid Token

```bash
curl -H "Authorization: Bearer <valid-descope-jwt>" http://localhost:8080/api/sample
# Expected: 200 OK
# Body: "This is a sample API endpoint."
```

## Controller Access to JWT

### Using @AuthenticationPrincipal

```java
@GetMapping("/api/sample")
public ResponseEntity<String> get(@AuthenticationPrincipal Jwt jwt) {
    // Access JWT claims
    String subject = jwt.getSubject();
    String issuer = jwt.getIssuer().toString();
    Instant expiration = jwt.getExpiresAt();
    Map<String, Object> claims = jwt.getClaims();
    
    return ResponseEntity.ok("Hello, " + subject);
}
```

### Using SecurityContextHolder

```java
@GetMapping("/api/sample")
public ResponseEntity<String> get() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
        Jwt jwt = jwtAuth.getToken();
        // Access JWT claims
    }
    return ResponseEntity.ok("Success");
}
```

## Comparison with .NET Approach

| Aspect | .NET | Spring Security |
|--------|------|-----------------|
| Token Extraction | Manual in controller | Automatic in filter |
| Validation Logic | Manual TokenValidator call | Automatic via NimbusJwtDecoder |
| Error Handling | Try-catch in controller | Automatic 401 response |
| Security Context | Manual claims extraction | SecurityContextHolder |
| Code Location | Scattered in controllers | Centralized in SecurityConfig |
| Testability | Requires mocking | `@WithMockUser` annotation |

## Verification

The JWT filter chain was verified by:

1. **Filter Chain Logging**: Debug logs show correct filter order
2. **Public Endpoints**: Accessible without authentication
3. **Protected Endpoints**: Return 401 without valid JWT
4. **Valid JWT**: Accepted and controller receives JWT claims
5. **Invalid JWT**: Rejected with appropriate error message

### Verification Commands

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Start the application
mvn spring-boot:run

# Test endpoints (in another terminal)
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/sample
```

## Summary

Task C3 completed the JWT filter chain integration by:

1. Verifying OAuth2 Resource Server configuration is correct
2. Confirming JWT validation works with Descope tokens
3. Documenting error handling for invalid tokens
4. Enabling logging for authentication events

The Spring Security filter chain provides automatic, centralized JWT authentication that replaces the manual validation code from the .NET application. This approach is more maintainable, testable, and follows security best practices.
