package com.descope.sample;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic Spring Boot application tests.
 * These tests verify that the application context loads correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "descope.project-id=test-project-id"
})
class DescopeSampleApplicationTests {

    @Test
    void contextLoads() {
        // Verify that the Spring application context loads successfully
    }
}
