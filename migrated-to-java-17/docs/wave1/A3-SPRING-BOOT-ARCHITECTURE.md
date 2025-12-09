# Task A3: Spring Boot Architecture Design

## Executive Summary

This document defines the target architecture for the migrated Descope Sample Application using Java 17 and Spring Boot 3.2. It provides a comprehensive mapping from the .NET Framework 4.8 architecture to Spring Boot patterns and conventions.

## Architecture Overview

### High-Level Comparison

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        .NET Framework 4.8                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│  Global.asax.cs          │  Application entry point, startup configuration  │
│  Web.config              │  XML-based configuration                         │
│  App_Start/*.cs          │  Route, filter, bundle configuration             │
│  Controllers/*.cs        │  MVC + Web API controllers                       │
│  Views/*.cshtml          │  Razor view templates                            │
│  Areas/HelpPage/         │  API documentation system                        │
│  TokenValidator.cs       │  Manual JWT validation                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Spring Boot 3.2                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│  DescopeSampleApplication.java  │  @SpringBootApplication entry point       │
│  application.properties         │  Properties-based configuration           │
│  config/*.java                  │  Security, Web, OpenAPI configuration     │
│  controller/*.java              │  @Controller + @RestController            │
│  templates/*.html               │  Thymeleaf templates                      │
│  Springdoc OpenAPI              │  Auto-generated API documentation         │
│  Spring Security OAuth2         │  Declarative JWT validation               │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Target Directory Structure

```
migrated-to-java-17/
├── pom.xml                                    # Maven build configuration
├── MIGRATION_PLAN.md                          # Migration documentation
├── docs/
│   └── wave1/                                 # Wave 1 deliverables
│       ├── A1-DOTNET-PROJECT-ANALYSIS.md
│       ├── A2-DEPENDENCY-MAPPING.md
│       └── A3-SPRING-BOOT-ARCHITECTURE.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── descope/
│   │   │           └── sample/
│   │   │               ├── DescopeSampleApplication.java    # Main class
│   │   │               ├── config/
│   │   │               │   ├── SecurityConfig.java          # Spring Security
│   │   │               │   ├── OpenApiConfig.java           # Swagger/OpenAPI
│   │   │               │   └── WebConfig.java               # Web MVC config
│   │   │               ├── controller/
│   │   │               │   ├── HomeController.java          # MVC controller
│   │   │               │   └── SampleApiController.java     # REST controller
│   │   │               ├── security/
│   │   │               │   └── TokenValidator.java          # JWT validation
│   │   │               ├── model/                           # DTOs (if needed)
│   │   │               └── service/                         # Business logic
│   │   └── resources/
│   │       ├── application.properties                       # Configuration
│   │       ├── static/
│   │       │   └── css/
│   │       │       └── site.css                             # Custom styles
│   │       └── templates/
│   │           ├── home.html                                # Landing page
│   │           ├── login.html                               # Login page
│   │           └── authenticated.html                       # Protected page
│   └── test/
│       └── java/
│           └── com/
│               └── descope/
│                   └── sample/
│                       ├── DescopeSampleApplicationTests.java
│                       ├── controller/
│                       │   ├── HomeControllerTest.java
│                       │   └── SampleApiControllerTest.java
│                       └── security/
│                           └── TokenValidatorTest.java
└── target/                                    # Build output (gitignored)
```

## Component Architecture

### 1. Application Entry Point

**Source (.NET):** `Global.asax.cs`

**Target (Spring Boot):** `DescopeSampleApplication.java`

```java
@SpringBootApplication
public class DescopeSampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(DescopeSampleApplication.class, args);
    }
}
```

**Mapping:**
| .NET Startup | Spring Boot Equivalent |
|--------------|------------------------|
| `Application_Start()` | `@SpringBootApplication` auto-configuration |
| `AreaRegistration.RegisterAllAreas()` | Not needed (no areas concept) |
| `GlobalConfiguration.Configure()` | `@Configuration` classes |
| `FilterConfig.RegisterGlobalFilters()` | `@ControllerAdvice` or Security filters |
| `RouteConfig.RegisterRoutes()` | `@RequestMapping` annotations |
| `BundleConfig.RegisterBundles()` | CDN links or WebJars |

### 2. Security Configuration

**Source (.NET):** Manual token validation in `SampleController.cs`

**Target (Spring Boot):** `SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${descope.project-id}")
    private String descopeProjectId;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwkSetUri(getJwksUri()))
            );
        return http.build();
    }

    private String getJwksUri() {
        return "https://api.descope.com/" + descopeProjectId + "/.well-known/jwks.json";
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**Security Flow Comparison:**

```
.NET Manual Validation:
┌─────────────────────────────────────────────────────────────────┐
│ Request → Controller → Extract Token → TokenValidator → Response │
└─────────────────────────────────────────────────────────────────┘

Spring Security OAuth2:
┌─────────────────────────────────────────────────────────────────────────────┐
│ Request → Security Filter Chain → JWT Filter → Controller → Response        │
│                                       │                                      │
│                                       ▼                                      │
│                              JWKS Validation                                 │
│                         (automatic via Spring Security)                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3. Controller Layer

#### MVC Controller

**Source (.NET):** `HomeController.cs`

```csharp
public class HomeController : Controller
{
    public ActionResult Index()
    {
        ViewBag.Title = "Home Page";
        return View();
    }
}
```

**Target (Spring Boot):** `HomeController.java`

```java
@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Home Page");
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/authenticated")
    public String authenticated(Model model) {
        return "authenticated";
    }
}
```

#### REST Controller

**Source (.NET):** `SampleController.cs`

```csharp
public class SampleController : ApiController
{
    public async Task<IHttpActionResult> Get()
    {
        // Manual token validation
        var authHeader = Request.Headers.Authorization;
        if (authHeader?.Scheme != "Bearer")
            return Unauthorized();
        
        var validator = new TokenValidator(Config.DescopeProjectId);
        var result = await validator.ValidateSession(authHeader.Parameter);
        
        if (result != null)
            return Ok("This is a sample API endpoint.");
        return Unauthorized();
    }
}
```

**Target (Spring Boot):** `SampleApiController.java`

```java
@RestController
@RequestMapping("/api")
@Tag(name = "Sample API", description = "Sample API endpoints demonstrating JWT authentication")
public class SampleApiController {

    @GetMapping("/sample")
    @Operation(summary = "Get sample data", description = "Returns sample data for authenticated users")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<String> getSample(@AuthenticationPrincipal Jwt jwt) {
        // JWT is automatically validated by Spring Security
        // Access claims via jwt.getClaims()
        return ResponseEntity.ok("This is a sample API endpoint.");
    }
}
```

**Key Differences:**
| Aspect | .NET | Spring Boot |
|--------|------|-------------|
| Authentication | Manual in controller | Automatic via Security filter |
| Token access | `Request.Headers.Authorization` | `@AuthenticationPrincipal Jwt` |
| Return type | `IHttpActionResult` | `ResponseEntity<T>` |
| Route definition | `[Route("api/[controller]")]` | `@RequestMapping("/api")` |

### 4. View Layer

#### Template Comparison

**Source (.NET Razor):** `Views/Home/Index.cshtml`

```html
@{
    ViewBag.Title = "Home Page";
}
<div class="jumbotron">
    <h1>Descope Sample App</h1>
    <p>@ViewBag.Message</p>
</div>
```

**Target (Thymeleaf):** `templates/home.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${title}">Home Page</title>
</head>
<body>
    <div class="jumbotron">
        <h1>Descope Sample App</h1>
        <p th:text="${message}">Welcome message</p>
    </div>
</body>
</html>
```

**Syntax Mapping:**
| Razor | Thymeleaf |
|-------|-----------|
| `@ViewBag.Property` | `${property}` |
| `@Model.Property` | `${model.property}` |
| `@if (condition) { }` | `th:if="${condition}"` |
| `@foreach (var item in items) { }` | `th:each="item : ${items}"` |
| `@Html.ActionLink()` | `th:href="@{/path}"` |
| `@Html.Partial()` | `th:replace="fragment"` |
| `@section Scripts { }` | `th:fragment` |
| `@RenderBody()` | `th:replace="~{::content}"` |

### 5. Configuration

**Source (.NET):** `Web.config`

```xml
<configuration>
  <appSettings>
    <add key="webpages:Version" value="3.0.0.0" />
    <add key="ClientValidationEnabled" value="true" />
  </appSettings>
  <system.web>
    <compilation debug="true" targetFramework="4.8" />
    <httpRuntime targetFramework="4.8" />
  </system.web>
</configuration>
```

**Target (Spring Boot):** `application.properties`

```properties
# Server Configuration
server.port=8080

# Descope Configuration
descope.project-id=${DESCOPE_PROJECT_ID:YOUR_DESCOPE_PROJECT_ID}

# Spring Security OAuth2 Resource Server
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://api.descope.com/${descope.project-id}/.well-known/jwks.json

# Thymeleaf Configuration
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# OpenAPI/Swagger Configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Actuator (Health checks)
management.endpoints.web.exposure.include=health,info

# Logging
logging.level.org.springframework.security=DEBUG
```

### 6. API Documentation

**Source (.NET):** HelpPage Area

```
Areas/HelpPage/
├── Controllers/HelpController.cs
├── Views/Help/Index.cshtml
├── Views/Help/Api.cshtml
└── ModelDescriptions/
```

**Target (Spring Boot):** Springdoc OpenAPI

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Descope Sample API")
                .version("1.0.0")
                .description("Sample API demonstrating Descope JWT authentication"))
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
```

**Documentation Endpoints:**
| Feature | .NET HelpPage | Spring Boot Springdoc |
|---------|---------------|----------------------|
| API List | `/Help` | `/swagger-ui.html` |
| API Details | `/Help/Api/{id}` | Swagger UI (interactive) |
| Model Schema | `/Help/ResourceModel/{name}` | `/v3/api-docs` (JSON) |
| Try It Out | Not available | Built-in |

## Request/Response Flow

### Authentication Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Spring Boot Authentication Flow                    │
└─────────────────────────────────────────────────────────────────────────────┘

1. Client Request
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ GET /api/sample                                                          │
   │ Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...           │
   └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
2. Security Filter Chain
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ BearerTokenAuthenticationFilter                                          │
   │   - Extracts Bearer token from Authorization header                      │
   │   - Creates BearerTokenAuthenticationToken                               │
   └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
3. JWT Decoder
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ NimbusJwtDecoder                                                         │
   │   - Fetches JWKS from Descope API (cached)                              │
   │   - Validates signature using public key                                 │
   │   - Validates claims (exp, iss, aud)                                    │
   └─────────────────────────────────────────────────────────────────────────┘
                                      │
                              ┌───────┴───────┐
                              │               │
                         Valid JWT       Invalid JWT
                              │               │
                              ▼               ▼
4a. Success                           4b. Failure
   ┌─────────────────────┐            ┌─────────────────────┐
   │ SecurityContext     │            │ 401 Unauthorized    │
   │ populated with      │            │ Response            │
   │ Authentication      │            └─────────────────────┘
   └─────────────────────┘
              │
              ▼
5. Controller Execution
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ @GetMapping("/api/sample")                                               │
   │ public ResponseEntity<String> getSample(@AuthenticationPrincipal Jwt) { │
   │     // Access JWT claims                                                 │
   │     return ResponseEntity.ok("Success");                                 │
   │ }                                                                        │
   └─────────────────────────────────────────────────────────────────────────┘
```

### MVC Request Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Spring MVC Request Flow                            │
└─────────────────────────────────────────────────────────────────────────────┘

1. Client Request
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ GET /login                                                               │
   └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
2. DispatcherServlet
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ - Receives all requests                                                  │
   │ - Delegates to HandlerMapping                                            │
   └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
3. Handler Mapping
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ RequestMappingHandlerMapping                                             │
   │ - Matches /login to HomeController.login()                              │
   └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
4. Controller
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ @GetMapping("/login")                                                    │
   │ public String login(Model model) {                                       │
   │     return "login";  // View name                                        │
   │ }                                                                        │
   └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
5. View Resolver
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ ThymeleafViewResolver                                                    │
   │ - Resolves "login" to templates/login.html                              │
   └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
6. Template Engine
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ SpringTemplateEngine                                                     │
   │ - Processes Thymeleaf template                                          │
   │ - Substitutes ${} expressions                                           │
   └─────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
7. Response
   ┌─────────────────────────────────────────────────────────────────────────┐
   │ HTML Response to Client                                                  │
   └─────────────────────────────────────────────────────────────────────────┘
```

## Error Handling

### Global Exception Handler

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("Access denied", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Internal error", ex.getMessage()));
    }
}

public record ErrorResponse(String error, String message) {}
```

## Testing Strategy

### Unit Tests

```java
@WebMvcTest(SampleApiController.class)
class SampleApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void getSample_authenticated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/sample"))
            .andExpect(status().isOk())
            .andExpect(content().string("This is a sample API endpoint."));
    }

    @Test
    void getSample_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/sample"))
            .andExpect(status().isUnauthorized());
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class DescopeSampleApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void homePage_returnsOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## Deployment Architecture

### Development

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Development Environment                            │
├─────────────────────────────────────────────────────────────────────────────┤
│  mvn spring-boot:run                                                         │
│  - Embedded Tomcat on port 8080                                             │
│  - Hot reload with spring-boot-devtools                                     │
│  - H2 console (if database needed)                                          │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Production

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Production Deployment                              │
├─────────────────────────────────────────────────────────────────────────────┤
│  Option 1: Executable JAR                                                    │
│  - java -jar descope-sample-app.jar                                         │
│  - Embedded Tomcat                                                          │
│                                                                              │
│  Option 2: Docker Container                                                  │
│  - FROM eclipse-temurin:17-jre                                              │
│  - COPY target/*.jar app.jar                                                │
│  - ENTRYPOINT ["java", "-jar", "/app.jar"]                                  │
│                                                                              │
│  Option 3: Cloud Platform                                                    │
│  - AWS Elastic Beanstalk                                                    │
│  - Google Cloud Run                                                         │
│  - Azure App Service                                                        │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Summary

The Spring Boot architecture provides a clean, modern approach to building the Descope Sample Application with several advantages over the .NET Framework 4.8 version:

1. **Simplified Security:** Spring Security OAuth2 Resource Server handles JWT validation declaratively
2. **Convention over Configuration:** Less boilerplate code with sensible defaults
3. **Modern Tooling:** Built-in support for OpenAPI, actuator endpoints, and DevTools
4. **Portable Deployment:** Single JAR file with embedded server
5. **Active Ecosystem:** Large community and extensive documentation

The migration preserves all functionality while modernizing the architecture and improving maintainability.
