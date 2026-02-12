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

import com.adjectivemonk2.greet.model.Greeting;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.jboss.logging.Logger;

@Path("hello")
@RunOnVirtualThread
public class GreetingResource {

  private final Logger logger;

  public GreetingResource(Logger logger) {
    this.logger = logger;
  }

  @GET
  public Greeting hello(@QueryParam("name") String name) {
    logger.info("Received request for hello endpoint with name: " + name);
    var resolvedName = name != null ? name : "Unnamed";
    return new Greeting("Hello, World from " + resolvedName + "!");
  }
}
