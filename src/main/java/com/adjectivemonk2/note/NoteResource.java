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

import com.adjectivemonk2.note.model.NoteData;
import com.adjectivemonk2.note.model.NoteResponse;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.jboss.logging.Logger;

@Path("notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RunOnVirtualThread
public class NoteResource {

  private final NoteService noteService;
  private final NoteSearchRepository noteSearchRepository;
  private final Logger logger;

  public NoteResource(
      NoteService noteService,
      NoteSearchRepository noteSearchRepository,
      Logger logger) {
    this.noteService = noteService;
    this.noteSearchRepository = noteSearchRepository;
    this.logger = logger;
  }

  @GET
  public List<NoteResponse> list() {
    logger.info("Fetching all notes");
    return noteService.findAll().stream()
        .map(NoteResponse::fromNote)
        .toList();
  }

  @GET
  @Path("{id}")
  public NoteResponse get(@PathParam("id") String id) {
    logger.info("Fetching note with id: " + id);
    var note = noteService.findById(id);
    if (note == null) {
      throw new NotFoundException("Note not found: " + id);
    }
    return NoteResponse.fromNote(note);
  }

  @GET
  @Path("search")
  public List<NoteResponse> search(@QueryParam("q") String q) throws Exception {
    logger.info("Searching notes with q=" + q);
    return noteSearchRepository.search(q).stream()
        .map(NoteResponse::fromDocument)
        .toList();
  }

  @POST
  public NoteResponse create(NoteData data) {
    logger.info("Creating note with title: " + data.title());
    var created = noteService.create(data);
    return NoteResponse.fromNote(created);
  }

  @PUT
  @Path("{id}")
  public NoteResponse update(@PathParam("id") String id, NoteData data) {
    logger.info("Updating note with id: " + id);
    var updated = noteService.update(id, data);
    if (updated == null) {
      throw new NotFoundException("Note not found: " + id);
    }
    return NoteResponse.fromNote(updated);
  }

  @DELETE
  @Path("{id}")
  public void delete(@PathParam("id") String id) {
    logger.info("Deleting note with id: " + id);
    var deleted = noteService.delete(id);
    if (!deleted) {
      throw new NotFoundException("Note not found: " + id);
    }
  }
}
