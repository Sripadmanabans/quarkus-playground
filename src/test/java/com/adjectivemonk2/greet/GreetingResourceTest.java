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

package com.adjectivemonk2.greet;

import static com.google.common.truth.Truth.assertThat;

import com.adjectivemonk2.greet.model.Greeting;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class GreetingResourceTest {

  @Inject
  GreetingResource greetingResource;

  @Test
  void testHelloEndpointWithoutQueryParameter() {
    var result = greetingResource.hello(null);
    assertThat(result).isEqualTo(new Greeting("Hello, World from Unnamed!"));
  }

  @Test
  void testHelloEndpointWithQueryParameter() {
    var result = greetingResource.hello("Alice");
    assertThat(result).isEqualTo(new Greeting("Hello, World from Alice!"));
  }
}
