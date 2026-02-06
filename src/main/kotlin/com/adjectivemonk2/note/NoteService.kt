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
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.jboss.logging.Logger

@ApplicationScoped
class NoteService(
  private val noteRepository: NoteRepository,
  private val noteSearchRepository: NoteSearchRepository,
  private val logger: Logger,
) {

  suspend fun findAll(): List<Note> = noteRepository.findAll()

  suspend fun findById(id: String): Note? = noteRepository.findById(id)

  suspend fun create(data: NoteData): Note {
    val note = noteRepository.create(data)
    try {
      noteSearchRepository.index(note)
    } catch (e: Exception) {
      logger.error("Failed to index note in Elasticsearch: ${note.id.toHexString()}", e)
    }
    return note
  }

  suspend fun update(id: String, data: NoteData): Note? {
    val note = noteRepository.update(id, data)
    if (note != null) {
      try {
        noteSearchRepository.index(note)
      } catch (e: Exception) {
        logger.error("Failed to re-index note in Elasticsearch: $id", e)
      }
    }
    return note
  }

  suspend fun delete(id: String): Boolean {
    val deleted = noteRepository.delete(id)
    if (deleted) {
      try {
        noteSearchRepository.delete(id)
      } catch (e: Exception) {
        logger.error("Failed to delete note from Elasticsearch: $id", e)
      }
    }
    return deleted
  }
}
