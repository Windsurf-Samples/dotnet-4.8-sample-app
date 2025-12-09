package com.descope.sample.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Controller for sample endpoints.
 * This replaces the SampleController.cs (Web API Controller) from the .NET application.
 * 
 * Migrated from: DescopeSampleApp/Controllers/SampleController.cs
 * 
 * .NET Web API Controller pattern:
 * - Inherits from ApiController
 * - Returns IHttpActionResult
 * - Manual JWT validation via TokenValidator
 * 
 * Spring REST Controller pattern:
 * - Annotated with @RestController
 * - Returns ResponseEntity<T>
 * - JWT validation handled by Spring Security filter chain
 * - Access to JWT claims via @AuthenticationPrincipal
 */
@RestController
@RequestMapping("/api/sample")
@Tag(name = "Sample", description = "Sample API endpoints demonstrating JWT authentication")
@SecurityRequirement(name = "Bearer Authentication")
public class SampleApiController {

    @GetMapping
    @Operation(
        summary = "Get sample data",
        description = "Returns a sample response. Requires valid JWT authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved sample data",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token"
        )
    })
    public ResponseEntity<String> get(@AuthenticationPrincipal Jwt jwt) {
        // JWT validation is handled automatically by Spring Security
        // The @AuthenticationPrincipal annotation provides access to the validated JWT
        
        // You can access JWT claims like this:
        // String subject = jwt.getSubject();
        // String issuer = jwt.getIssuer().toString();
        // Map<String, Object> claims = jwt.getClaims();
        
        return ResponseEntity.ok("This is a sample API endpoint.");
    }
}
