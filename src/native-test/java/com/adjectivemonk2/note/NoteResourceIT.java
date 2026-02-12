/*
 * Copyright (C) 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adjectivemonk2.note;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
class NoteResourceIT {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String toJson(String title, String content) throws JsonProcessingException {
    return objectMapper.writeValueAsString(Map.of("title", title, "content", content));
  }

  @Test
  void testCreateNote() throws Exception {
    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(toJson("IT Note", "IT Content"))
        .when().post("/notes")
        .then()
        .statusCode(200)
        .body("title", CoreMatchers.is("IT Note"))
        .body("content", CoreMatchers.is("IT Content"))
        .body("id", CoreMatchers.notNullValue());
  }

  @Test
  void testListNotes() throws Exception {
    // Create a note first
    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(toJson("List Test", "Content"))
        .when().post("/notes")
        .then()
        .statusCode(200);

    // List should return at least one note
    RestAssured.given()
        .when().get("/notes")
        .then()
        .statusCode(200);
  }

  @Test
  void testGetNote() throws Exception {
    // Create a note first
    var id = RestAssured.given()
        .contentType(ContentType.JSON)
        .body(toJson("Get Test", "Content"))
        .when().post("/notes")
        .then()
        .statusCode(200)
        .extract().path("id").toString();

    // Get the created note
    RestAssured.given()
        .when().get("/notes/" + id)
        .then()
        .statusCode(200)
        .body("id", CoreMatchers.is(id))
        .body("title", CoreMatchers.is("Get Test"));
  }

  @Test
  void testGetNoteNotFound() {
    RestAssured.given()
        .when().get("/notes/000000000000000000000000")
        .then()
        .statusCode(404);
  }

  @Test
  void testUpdateNote() throws Exception {
    // Create a note first
    var id = RestAssured.given()
        .contentType(ContentType.JSON)
        .body(toJson("Original", "Original"))
        .when().post("/notes")
        .then()
        .statusCode(200)
        .extract().path("id").toString();

    // Update the note
    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(toJson("Updated", "Updated Content"))
        .when().put("/notes/" + id)
        .then()
        .statusCode(200)
        .body("id", CoreMatchers.is(id))
        .body("title", CoreMatchers.is("Updated"))
        .body("content", CoreMatchers.is("Updated Content"));
  }

  @Test
  void testUpdateNoteNotFound() throws Exception {
    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(toJson("Title", "Content"))
        .when().put("/notes/000000000000000000000000")
        .then()
        .statusCode(404);
  }

  @Test
  void testDeleteNote() throws Exception {
    // Create a note first
    var id = RestAssured.given()
        .contentType(ContentType.JSON)
        .body(toJson("To Delete", "Content"))
        .when().post("/notes")
        .then()
        .statusCode(200)
        .extract().path("id").toString();

    // Delete the note
    RestAssured.given()
        .when().delete("/notes/" + id)
        .then()
        .statusCode(204);

    // Verify it's gone
    RestAssured.given()
        .when().get("/notes/" + id)
        .then()
        .statusCode(404);
  }

  @Test
  void testDeleteNoteNotFound() {
    RestAssured.given()
        .when().delete("/notes/000000000000000000000000")
        .then()
        .statusCode(404);
  }

  @Test
  void testSearchRequiresAtLeastOneParam() {
    RestAssured.given()
        .when().get("/notes/search")
        .then()
        .statusCode(400);
  }

  @Test
  void testSearchByQ() throws Exception {
    // Create notes
    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(toJson("Kotlin Guide", "Learn Kotlin"))
        .when().post("/notes")
        .then()
        .statusCode(200);

    RestAssured.given()
        .contentType(ContentType.JSON)
        .body(toJson("Java Guide", "Learn Java"))
        .when().post("/notes")
        .then()
        .statusCode(200);

    // Poll until OpenSearch has refreshed and the document is searchable
    Awaitility.await()
        .atMost(5, TimeUnit.SECONDS)
        .pollInterval(200, TimeUnit.MILLISECONDS)
        .untilAsserted(() ->
            RestAssured.given()
                .queryParam("q", "Kotlin")
                .when().get("/notes/search")
                .then()
                .statusCode(200)
                .body("size()", CoreMatchers.is(1))
                .body("[0].title", CoreMatchers.is("Kotlin Guide")));
  }
}
