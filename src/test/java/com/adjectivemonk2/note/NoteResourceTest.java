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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.adjectivemonk2.note.model.Note;
import com.adjectivemonk2.note.model.NoteData;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;

@QuarkusTest
class NoteResourceTest {

  @Inject
  NoteResource noteResource;

  @Inject
  MongoClient mongoClient;

  @Inject
  OpenSearchClient openSearchClient;

  @Inject
  @ConfigProperty(name = "quarkus.mongodb.database")
  String databaseName;

  @AfterEach
  void cleanup() throws Exception {
    mongoClient.getDatabase(databaseName).getCollection("notes", Note.class).deleteMany(Filters.empty());
    var indexExists = openSearchClient.indices().exists(b -> b.index("notes")).value();
    if (indexExists) {
      openSearchClient.deleteByQuery(b -> b.index("notes").query(q -> q.matchAll(m -> m)));
      openSearchClient.indices().refresh(b -> b.index("notes"));
    }
  }

  private void refreshOpenSearch() throws Exception {
    openSearchClient.indices().refresh(b -> b.index("notes"));
  }

  @Test
  void listShouldReturnEmptyListWhenNoNotesExist() {
    var result = noteResource.list();
    assertThat(result).isEmpty();
  }

  @Test
  void listShouldReturnAllNotes() {
    noteResource.create(new NoteData("Note 1", "Content 1"));
    noteResource.create(new NoteData("Note 2", "Content 2"));

    var result = noteResource.list();

    assertThat(result).hasSize(2);
    var titles = result.stream().map(r -> r.title()).toList();
    assertThat(titles).containsExactly("Note 1", "Note 2");
  }

  @Test
  void createShouldReturnNoteResponseWithId() {
    var data = new NoteData("New Note", "New Content");

    var result = noteResource.create(data);

    assertThat(result.title()).isEqualTo("New Note");
    assertThat(result.content()).isEqualTo("New Content");
  }

  @Test
  void getShouldReturnNoteWhenItExists() {
    var created = noteResource.create(new NoteData("Get Me", "Content"));

    var result = noteResource.get(created.id());

    assertThat(result.id()).isEqualTo(created.id());
    assertThat(result.title()).isEqualTo("Get Me");
  }

  @Test
  void getShouldThrowNotFoundExceptionWhenNoteDoesNotExist() {
    assertThrows(NotFoundException.class, () ->
        noteResource.get("000000000000000000000000"));
  }

  @Test
  void updateShouldModifyNoteAndReturnUpdatedResponse() {
    var created = noteResource.create(new NoteData("Original", "Original Content"));
    var updateData = new NoteData("Updated", "Updated Content");

    var result = noteResource.update(created.id(), updateData);

    assertThat(result.id()).isEqualTo(created.id());
    assertThat(result.title()).isEqualTo("Updated");
    assertThat(result.content()).isEqualTo("Updated Content");
  }

  @Test
  void updateShouldThrowNotFoundExceptionWhenNoteDoesNotExist() {
    assertThrows(NotFoundException.class, () ->
        noteResource.update("000000000000000000000000", new NoteData("Title", "Content")));
  }

  @Test
  void deleteShouldRemoveNoteSuccessfully() {
    var created = noteResource.create(new NoteData("To Delete", "Content"));

    noteResource.delete(created.id());

    assertThrows(NotFoundException.class, () ->
        noteResource.get(created.id()));
  }

  @Test
  void deleteShouldThrowNotFoundExceptionWhenNoteDoesNotExist() {
    assertThrows(NotFoundException.class, () ->
        noteResource.delete("000000000000000000000000"));
  }

  @Test
  void searchByQShouldFindNotesMatchingTitle() throws Exception {
    noteResource.create(new NoteData("Kotlin Guide", "Learn the basics"));
    noteResource.create(new NoteData("Java Guide", "Learn Java basics"));
    refreshOpenSearch();

    var results = noteResource.search("Kotlin");

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().title()).isEqualTo("Kotlin Guide");
  }

  @Test
  void searchByQShouldFindNotesMatchingContent() throws Exception {
    noteResource.create(new NoteData("The Guide", "Learn Kotlin basics"));
    noteResource.create(new NoteData("Java Guide", "Learn Java basics"));
    refreshOpenSearch();

    var results = noteResource.search("Kotlin");

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().title()).isEqualTo("The Guide");
  }
}
