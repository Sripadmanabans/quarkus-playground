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
import com.adjectivemonk2.note.model.NoteDocument;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Objects;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.search.Hit;

@ApplicationScoped
public class NoteSearchRepository {

  private static final String INDEX_NAME = "notes";

  private final OpenSearchClient client;
  private final Logger logger;

  public NoteSearchRepository(OpenSearchClient client, Logger logger) {
    this.client = client;
    this.logger = logger;
  }

  public IndexResponse index(Note note) throws Exception {
    var id = note.id().toHexString();
    logger.info("Indexing note in OpenSearch: " + id);
    return client.index(builder ->
        builder
            .index(INDEX_NAME)
            .id(id)
            .document(new NoteDocument(id, note.title(), note.content())));
  }

  public DeleteResponse delete(String id) throws Exception {
    logger.info("Deleting note from OpenSearch: " + id);
    return client.delete(builder -> builder.index(INDEX_NAME).id(id));
  }

  public List<NoteDocument> search(String q) throws Exception {
    var qQuery = Query.of(query ->
        query.multiMatch(mm -> mm.query(q).fields("title", "content")));

    var response = client.search(
        request -> request.index(INDEX_NAME).query(qQuery),
        NoteDocument.class);

    var hits = response.hits().hits();
    logger.info("Found " + hits.size() + " notes for q=" + q);
    return hits.stream()
        .map(Hit::source)
        .filter(Objects::nonNull)
        .toList();
  }
}
