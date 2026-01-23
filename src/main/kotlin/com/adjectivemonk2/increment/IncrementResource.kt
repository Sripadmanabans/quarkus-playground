package com.adjectivemonk2.increment

import com.adjectivemonk2.increment.model.Increment
import jakarta.inject.Inject
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam

@Path("/increment")
class IncrementResource @Inject constructor(private val repository: IncrementRepository) {
  @GET
  suspend fun keys(): List<String> {
    return repository.keys()
  }

  @POST
  suspend fun create(increment: Increment): Increment {
    repository.set(increment.key, increment.value)
    return increment
  }

  @GET
  @Path("/{key}")
  suspend fun get(key: String): Increment {
    return Increment(key, repository.get(key))
  }

  @PUT
  @Path("/{key}")
  suspend fun update(key: String, value: Long) {
    repository.increment(key, value)
  }

  @DELETE
  @Path("/{key}")
  suspend fun delete(key: String) {
    repository.delete(key)
  }
}
