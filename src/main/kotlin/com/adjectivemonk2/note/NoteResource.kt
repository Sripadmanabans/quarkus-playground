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

import com.adjectivemonk2.note.model.Note
import com.adjectivemonk2.note.model.NoteData
import com.adjectivemonk2.note.model.NoteDocument
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import kotlinx.serialization.Serializable
import org.jboss.logging.Logger

@Path("notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class NoteResource(
  private val noteService: NoteService,
  private val noteSearchRepository: NoteSearchRepository,
  private val logger: Logger,
) {

  @GET
  suspend fun list(): List<NoteResponse> {
    logger.info("Fetching all notes")
    return noteService.findAll().map { it.toResponse() }
  }

  @GET
  @Path("{id}")
  suspend fun get(@PathParam("id") id: String): NoteResponse {
    logger.info("Fetching note with id: $id")
    val note = noteService.findById(id) ?: throw NotFoundException("Note not found: $id")
    return note.toResponse()
  }

  @GET
  @Path("search")
  suspend fun search(@QueryParam("q") q: String): List<NoteResponse> {
    logger.info("Searching notes with q=$q")
    return noteSearchRepository.search(q).map { it.toResponse() }
  }

  @POST
  suspend fun create(data: NoteData): NoteResponse {
    logger.info("Creating note with title: ${data.title}")
    val created = noteService.create(data)
    return created.toResponse()
  }

  @PUT
  @Path("{id}")
  suspend fun update(@PathParam("id") id: String, data: NoteData): NoteResponse {
    logger.info("Updating note with id: $id")
    val updated = noteService.update(id, data) ?: throw NotFoundException("Note not found: $id")
    return updated.toResponse()
  }

  @DELETE
  @Path("{id}")
  suspend fun delete(@PathParam("id") id: String) {
    logger.info("Deleting note with id: $id")
    val deleted = noteService.delete(id)
    if (!deleted) {
      throw NotFoundException("Note not found: $id")
    }
  }

  private fun Note.toResponse() = NoteResponse(
    id = id.toHexString(),
    title = title,
    content = content,
  )

  private fun NoteDocument.toResponse() = NoteResponse(
    id = id,
    title = title,
    content = content,
  )
}

@Serializable
data class NoteResponse(val id: String, val title: String, val content: String)
