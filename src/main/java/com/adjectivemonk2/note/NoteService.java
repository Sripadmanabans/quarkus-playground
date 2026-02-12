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

import com.adjectivemonk2.note.model.Note;
import com.adjectivemonk2.note.model.NoteData;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class NoteService {

  private final NoteRepository noteRepository;
  private final NoteSearchRepository noteSearchRepository;
  private final Logger logger;

  public NoteService(
      NoteRepository noteRepository,
      NoteSearchRepository noteSearchRepository,
      Logger logger) {
    this.noteRepository = noteRepository;
    this.noteSearchRepository = noteSearchRepository;
    this.logger = logger;
  }

  public List<Note> findAll() {
    return noteRepository.findAll();
  }

  public Note findById(String id) {
    return noteRepository.findById(id);
  }

  public Note create(NoteData data) {
    var note = noteRepository.create(data);
    try {
      noteSearchRepository.index(note);
    } catch (Exception e) {
      logger.error("Failed to index note in OpenSearch: " + note.id().toHexString(), e);
    }
    return note;
  }

  public Note update(String id, NoteData data) {
    var note = noteRepository.update(id, data);
    if (note != null) {
      try {
        noteSearchRepository.index(note);
      } catch (Exception e) {
        logger.error("Failed to re-index note in OpenSearch: " + id, e);
      }
    }
    return note;
  }

  public boolean delete(String id) {
    var deleted = noteRepository.delete(id);
    if (deleted) {
      try {
        noteSearchRepository.delete(id);
      } catch (Exception e) {
        logger.error("Failed to delete note from OpenSearch: " + id, e);
      }
    }
    return deleted;
  }
}
