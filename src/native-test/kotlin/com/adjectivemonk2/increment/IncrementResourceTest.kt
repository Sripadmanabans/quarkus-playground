package com.adjectivemonk2.increment

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

@QuarkusTest
class IncrementResourceTest {
  @Test
  fun testRedisOperations() {
    // verify that we have nothing
    RestAssured.given()
      .accept(ContentType.JSON)
      .`when`()
      .get("/increments")
      .then()
      .statusCode(200)
      .body("size()", Matchers.`is`(0))

    // create a first increment key with an initial value of 0
    RestAssured.given()
      .contentType(ContentType.JSON)
      .accept(ContentType.JSON)
      .body("{\"key\":\"first-key\",\"value\":0}")
      .`when`()
      .post("/increments")
      .then()
      .statusCode(200)
      .body("key", Matchers.`is`("first-key"))
      .body("value", Matchers.`is`(0))

    // create a second increment key with an initial value of 10
    RestAssured.given()
      .contentType(ContentType.JSON)
      .accept(ContentType.JSON)
      .body("{\"key\":\"second-key\",\"value\":10}")
      .`when`()
      .post("/increments")
      .then()
      .statusCode(200)
      .body("key", Matchers.`is`("second-key"))
      .body("value", Matchers.`is`(10))

    // increment first key by 1
    RestAssured.given()
      .contentType(ContentType.JSON)
      .body("1")
      .`when`()
      .put("/increments/first-key")
      .then()
      .statusCode(204)

    // verify that key has been incremented
    RestAssured.given()
      .accept(ContentType.JSON)
      .`when`()
      .get("/increments/first-key")
      .then()
      .statusCode(200)
      .body("key", Matchers.`is`("first-key"))
      .body("value", Matchers.`is`(1))

    // increment second key by 1000
    RestAssured.given()
      .contentType(ContentType.JSON)
      .body("1000")
      .`when`()
      .put("/increments/second-key")
      .then()
      .statusCode(204)

    // verify that key has been incremented
    RestAssured.given()
      .accept(ContentType.JSON)
      .`when`()
      .get("/increments/second-key")
      .then()
      .statusCode(200)
      .body("key", Matchers.`is`("second-key"))
      .body("value", Matchers.`is`(1010))

    // verify that we have two keys in registered
    RestAssured.given()
      .accept(ContentType.JSON)
      .`when`()
      .get("/increments")
      .then()
      .statusCode(200)
      .body("size()", Matchers.`is`(2))

    // delete first key
    RestAssured.given()
      .accept(ContentType.JSON)
      .`when`()
      .delete("/increments/first-key")
      .then()
      .statusCode(204)

    // verify that we have one key left after deletion
    RestAssured.given()
      .accept(ContentType.JSON)
      .`when`()
      .get("/increments")
      .then()
      .statusCode(200)
      .body("size()", Matchers.`is`(1))

    // delete second key
    RestAssured.given()
      .accept(ContentType.JSON)
      .`when`()
      .delete("/increments/second-key")
      .then()
      .statusCode(204)

    // verify that there is no key left
    RestAssured.given()
      .accept(ContentType.JSON)
      .`when`()
      .get("/increments")
      .then()
      .statusCode(200)
      .body("size()", Matchers.`is`(0))
  }
}
