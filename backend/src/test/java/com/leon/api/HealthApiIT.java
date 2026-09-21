package com.leon.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.leon.integration.IntegrationTestBase;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Section 67. Health must reflect the database, not just "the JVM is up". */
class HealthApiIT extends IntegrationTestBase {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void healthIsUpWhenPostgresIsReachable() {
        given()
        .when().get("/actuator/health")
        .then().statusCode(200)
               .body("status", equalTo("UP"));
    }

    @Test
    void theOpenApiContractIsServed() {
        given()
        .when().get("/openapi.json")
        .then().statusCode(200);
    }
}
