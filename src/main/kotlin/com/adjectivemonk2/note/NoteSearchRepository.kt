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

import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch._types.query_dsl.Query
import org.opensearch.client.opensearch.core.DeleteResponse
import org.opensearch.client.opensearch.core.IndexResponse
import com.adjectivemonk2.note.model.Note
import com.adjectivemonk2.note.model.NoteDocument
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jboss.logging.Logger

@ApplicationScoped
class NoteSearchRepository(
  private val client: OpenSearchClient,
  private val logger: Logger,
) {

  suspend fun index(note: Note): IndexResponse? {
    return withContext(Dispatchers.IO) {
      val id = note.id.toHexString()
      logger.info("Indexing note in OpenSearch: $id")
      client.index { builder ->
        builder
          .index(INDEX_NAME)
          .id(id)
          .document(NoteDocument(id = id, title = note.title, content = note.content))
      }
    }
  }

  suspend fun delete(id: String): DeleteResponse? {
    return withContext(Dispatchers.IO) {
      logger.info("Deleting note from OpenSearch: $id")
      client.delete { it.index(INDEX_NAME).id(id) }
    }
  }

  suspend fun search(q: String): List<NoteDocument> {
    return withContext(Dispatchers.IO) {
      val qQuery = Query.of { query ->
        query.multiMatch { mm ->
          mm.query(q).fields("title", "content")
        }
      }

      val response = client.search(
        { request -> request.index(INDEX_NAME).query(qQuery) },
        NoteDocument::class.java,
      )

      val hits = response.hits().hits()
      logger.info("Found ${hits.size} notes for q=$q")
      hits.mapNotNull { it.source() }
    }
  }

  private companion object {
    const val INDEX_NAME = "notes"
  }
}
