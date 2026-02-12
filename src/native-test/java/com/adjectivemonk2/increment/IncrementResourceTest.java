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

package com.adjectivemonk2.increment;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@QuarkusTest
class IncrementResourceTest {

  @Test
  void testRedisOperations() {
    // verify that we have nothing
    RestAssured.given()
        .accept(ContentType.JSON)
        .when()
        .get("/increment")
        .then()
        .statusCode(200)
        .body("size()", Matchers.is(0));

    // create a first increment key with an initial value of 0
    RestAssured.given()
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .body("{\"key\":\"first-key\",\"value\":0}")
        .when()
        .post("/increment")
        .then()
        .statusCode(200)
        .body("key", Matchers.is("first-key"))
        .body("value", Matchers.is(0));

    // create a second increment key with an initial value of 10
    RestAssured.given()
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .body("{\"key\":\"second-key\",\"value\":10}")
        .when()
        .post("/increment")
        .then()
        .statusCode(200)
        .body("key", Matchers.is("second-key"))
        .body("value", Matchers.is(10));

    // increment first key by 1
    RestAssured.given()
        .contentType(ContentType.JSON)
        .body("1")
        .when()
        .put("/increment/first-key")
        .then()
        .statusCode(204);

    // verify that key has been incremented
    RestAssured.given()
        .accept(ContentType.JSON)
        .when()
        .get("/increment/first-key")
        .then()
        .statusCode(200)
        .body("key", Matchers.is("first-key"))
        .body("value", Matchers.is(1));

    // increment second key by 1000
    RestAssured.given()
        .contentType(ContentType.JSON)
        .body("1000")
        .when()
        .put("/increment/second-key")
        .then()
        .statusCode(204);

    // verify that key has been incremented
    RestAssured.given()
        .accept(ContentType.JSON)
        .when()
        .get("/increment/second-key")
        .then()
        .statusCode(200)
        .body("key", Matchers.is("second-key"))
        .body("value", Matchers.is(1010));

    // verify that we have two keys registered
    RestAssured.given()
        .accept(ContentType.JSON)
        .when()
        .get("/increment")
        .then()
        .statusCode(200)
        .body("size()", Matchers.is(2));

    // delete first key
    RestAssured.given()
        .accept(ContentType.JSON)
        .when()
        .delete("/increment/first-key")
        .then()
        .statusCode(204);

    // verify that we have one key left after deletion
    RestAssured.given()
        .accept(ContentType.JSON)
        .when()
        .get("/increment")
        .then()
        .statusCode(200)
        .body("size()", Matchers.is(1));

    // delete second key
    RestAssured.given()
        .accept(ContentType.JSON)
        .when()
        .delete("/increment/second-key")
        .then()
        .statusCode(204);

    // verify that there is no key left
    RestAssured.given()
        .accept(ContentType.JSON)
        .when()
        .get("/increment")
        .then()
        .statusCode(200)
        .body("size()", Matchers.is(0));
  }
}
