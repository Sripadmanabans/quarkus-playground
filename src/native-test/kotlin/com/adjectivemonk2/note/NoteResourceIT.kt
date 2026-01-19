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

package com.adjectivemonk2.note

import com.adjectivemonk2.note.model.NoteData
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Test

@QuarkusIntegrationTest
class NoteResourceIT {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun testCreateNote() {
    val requestBody = json.encodeToString(NoteData.serializer(), NoteData("IT Note", "IT Content"))

    RestAssured.given()
      .contentType(ContentType.JSON)
      .body(requestBody)
      .`when`().post("/notes")
      .then()
      .statusCode(200)
      .body("title", CoreMatchers.`is`("IT Note"))
      .body("content", CoreMatchers.`is`("IT Content"))
      .body("id", CoreMatchers.notNullValue())
  }

  @Test
  fun testListNotes() {
    // Create a note first
    val requestBody = json.encodeToString(NoteData.serializer(), NoteData("List Test", "Content"))
    RestAssured.given()
      .contentType(ContentType.JSON)
      .body(requestBody)
      .`when`().post("/notes")
      .then()
      .statusCode(200)

    // List should return at least one note
    RestAssured.given()
      .`when`().get("/notes")
      .then()
      .statusCode(200)
  }

  @Test
  fun testGetNote() {
    // Create a note first
    val requestBody = json.encodeToString(NoteData.serializer(), NoteData("Get Test", "Content"))
    val id = RestAssured.given()
      .contentType(ContentType.JSON)
      .body(requestBody)
      .`when`().post("/notes")
      .then()
      .statusCode(200)
      .extract().path<String>("id")

    // Get the created note
    RestAssured.given()
      .`when`().get("/notes/$id")
      .then()
      .statusCode(200)
      .body("id", CoreMatchers.`is`(id))
      .body("title", CoreMatchers.`is`("Get Test"))
  }

  @Test
  fun testGetNoteNotFound() {
    RestAssured.given()
      .`when`().get("/notes/000000000000000000000000")
      .then()
      .statusCode(404)
  }

  @Test
  fun testUpdateNote() {
    // Create a note first
    val createBody = json.encodeToString(NoteData.serializer(), NoteData("Original", "Original"))
    val id = RestAssured.given()
      .contentType(ContentType.JSON)
      .body(createBody)
      .`when`().post("/notes")
      .then()
      .statusCode(200)
      .extract().path<String>("id")

    // Update the note
    val updateBody = json.encodeToString(NoteData.serializer(), NoteData("Updated", "Updated Content"))
    RestAssured.given()
      .contentType(ContentType.JSON)
      .body(updateBody)
      .`when`().put("/notes/$id")
      .then()
      .statusCode(200)
      .body("id", CoreMatchers.`is`(id))
      .body("title", CoreMatchers.`is`("Updated"))
      .body("content", CoreMatchers.`is`("Updated Content"))
  }

  @Test
  fun testUpdateNoteNotFound() {
    val updateBody = json.encodeToString(NoteData.serializer(), NoteData("Title", "Content"))
    RestAssured.given()
      .contentType(ContentType.JSON)
      .body(updateBody)
      .`when`().put("/notes/000000000000000000000000")
      .then()
      .statusCode(404)
  }

  @Test
  fun testDeleteNote() {
    // Create a note first
    val requestBody = json.encodeToString(NoteData.serializer(), NoteData("To Delete", "Content"))
    val id = RestAssured.given()
      .contentType(ContentType.JSON)
      .body(requestBody)
      .`when`().post("/notes")
      .then()
      .statusCode(200)
      .extract().path<String>("id")

    // Delete the note
    RestAssured.given()
      .`when`().delete("/notes/$id")
      .then()
      .statusCode(204)

    // Verify it's gone
    RestAssured.given()
      .`when`().get("/notes/$id")
      .then()
      .statusCode(404)
  }

  @Test
  fun testDeleteNoteNotFound() {
    RestAssured.given()
      .`when`().delete("/notes/000000000000000000000000")
      .then()
      .statusCode(404)
  }
}
