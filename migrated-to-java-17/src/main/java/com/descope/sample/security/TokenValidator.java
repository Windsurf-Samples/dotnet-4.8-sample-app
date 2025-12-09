package com.descope.sample.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * JWT Token Validator for Descope authentication.
 * This is a direct port of the TokenValidator.cs from the .NET application.
 * 
 * Migrated from: DescopeSampleApp/TokenValidator.cs
 * 
 * Note: In Spring Boot, JWT validation is typically handled by Spring Security's
 * OAuth2 Resource Server. This class is provided for reference and for cases
 * where manual token validation is needed.
 * 
 * .NET Implementation used:
 * - jose-jwt library for JWT decoding
 * - HttpClient for fetching JWKS
 * - JwkSet for managing public keys
 * 
 * Java Implementation uses:
 * - Auth0 java-jwt library for JWT decoding
 * - Auth0 jwks-rsa library for fetching JWKS
 * - JwkProvider for managing public keys with caching
 */
@Component
public class TokenValidator {

    private final String projectId;
    private final JwkProvider jwkProvider;

    public TokenValidator(@Value("${descope.project-id}") String projectId) {
        this.projectId = projectId;
        
        // Initialize JWK provider with caching (similar to .NET's GetPublicKeyAsync)
        try {
            String jwksUrl = String.format("https://api.descope.com/%s/.well-known/jwks.json", projectId);
            this.jwkProvider = new JwkProviderBuilder(new URL(jwksUrl))
                .cached(10, 24, TimeUnit.HOURS)  // Cache up to 10 keys for 24 hours
                .rateLimited(10, 1, TimeUnit.MINUTES)  // Rate limit: 10 requests per minute
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWK provider", e);
        }
    }

    /**
     * Validates a session token and returns the decoded JWT.
     * This is equivalent to the ValidateSession method in the .NET TokenValidator.
     * 
     * @param sessionToken The JWT session token to validate
     * @return The decoded and validated JWT
     * @throws Exception if validation fails
     */
    public DecodedJWT validateSession(String sessionToken) throws Exception {
        try {
            // Decode the token to get the key ID (kid)
            DecodedJWT decodedJWT = JWT.decode(sessionToken);
            String keyId = decodedJWT.getKeyId();
            
            // Fetch the public key from JWKS
            Jwk jwk = jwkProvider.get(keyId);
            RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
            
            // Create verifier with the public key
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = JWT.require(algorithm)
                .acceptLeeway(60)  // Allow 60 seconds of clock skew
                .build();
            
            // Verify and return the token
            return verifier.verify(sessionToken);
            
        } catch (Exception ex) {
            System.err.println("Error parsing and verifying token: " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Verifies if a token has expired.
     * This is equivalent to the VerifyTokenExpiration method in the .NET TokenValidator.
     * 
     * @param sessionToken The JWT session token to check
     * @return true if the token is still valid, false if expired
     */
    public boolean verifyTokenExpiration(String sessionToken) {
        try {
            DecodedJWT decodedJWT = JWT.decode(sessionToken);
            Instant expirationTime = decodedJWT.getExpiresAtAsInstant();
            return expirationTime != null && expirationTime.isAfter(Instant.now());
        } catch (Exception ex) {
            System.err.println("Error verifying token expiration: " + ex.getMessage());
            return false;
        }
    }
}
