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

import com.adjectivemonk2.increment.model.Increment;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;

@Path("/increment")
@RunOnVirtualThread
public class IncrementResource {

  private final IncrementRepository repository;

  public IncrementResource(IncrementRepository repository) {
    this.repository = repository;
  }

  @GET
  public List<String> keys() {
    return repository.keys();
  }

  @POST
  public Increment create(Increment increment) {
    repository.set(increment.key(), increment.value());
    return increment;
  }

  @GET
  @Path("/{key}")
  public Increment get(@PathParam("key") String key) {
    return new Increment(key, repository.get(key));
  }

  @PUT
  @Path("/{key}")
  public void update(@PathParam("key") String key, long value) {
    repository.increment(key, value);
  }

  @DELETE
  @Path("/{key}")
  public void delete(@PathParam("key") String key) {
    repository.delete(key);
  }
}
