package com.leon.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.leon.integration.IntegrationTestBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Section 42 and 43. The API contract exercised over real HTTP. */
class ProjectApiIT extends IntegrationTestBase {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void listsAllProjects() {
        given().contentType(ContentType.JSON)
        .when().get("/api/v1/projects")
        .then().statusCode(200)
               .body("size()", greaterThan(0))
               .body("slug", hasItem("keyguard"));
    }

    @Test
    void filtersByFeatured() {
        given().queryParam("featured", true)
        .when().get("/api/v1/projects")
        .then().statusCode(200)
               .body("featured", everyItem(is(true)));
    }

    @Test
    void returnsASingleProjectBySlug() {
        given()
        .when().get("/api/v1/projects/navigation-assistant")
        .then().statusCode(200)
               .body("slug", equalTo("navigation-assistant"))
               .body("technologies", hasItem("Kotlin"));
    }

    @Test
    void missingProjectIsA404WithAStructuredBody() {
        given()
        .when().get("/api/v1/projects/no-such-project")
        .then().statusCode(404)
               .body("status", equalTo(404))
               .body("detail", containsString("no-such-project"))
               .body("requestId", not(emptyOrNullString()));
    }

    @Test
    void anUnparseableFilterIsRejectedAsClientErrorNotServerError() {
        given().queryParam("status", "NOT_A_STATUS")
        .when().get("/api/v1/projects")
        .then().statusCode(400);
    }

    @Test
    void everyResponseCarriesARequestId() {
        given()
        .when().get("/api/v1/projects")
        .then().statusCode(200)
               .header("X-Request-ID", not(emptyOrNullString()));
    }
}
