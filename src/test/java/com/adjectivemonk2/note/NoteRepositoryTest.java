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

import com.adjectivemonk2.note.model.Note;
import com.adjectivemonk2.note.model.NoteData;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class NoteRepositoryTest {

  @Inject
  NoteRepository noteRepository;

  @Inject
  MongoClient mongoClient;

  @Inject
  @ConfigProperty(name = "quarkus.mongodb.database")
  String databaseName;

  @AfterEach
  void cleanup() {
    mongoClient.getDatabase(databaseName).getCollection("notes", Note.class).deleteMany(Filters.empty());
  }

  @Test
  void createShouldPersistNoteAndReturnItWithAnId() {
    var data = new NoteData("Test Note", "Test Content");

    var created = noteRepository.create(data);

    assertThat(created.title()).isEqualTo("Test Note");
    assertThat(created.content()).isEqualTo("Test Content");
  }

  @Test
  void findAllShouldReturnAllPersistedNotes() {
    noteRepository.create(new NoteData("Note 1", "Content 1"));
    noteRepository.create(new NoteData("Note 2", "Content 2"));

    var notes = noteRepository.findAll();

    assertThat(notes).hasSize(2);
    var titles = notes.stream().map(Note::title).toList();
    assertThat(titles).containsExactly("Note 1", "Note 2");
  }

  @Test
  void findByIdShouldReturnNoteWhenItExists() {
    var created = noteRepository.create(new NoteData("Find Me", "Content"));

    var found = noteRepository.findById(created.id().toHexString());

    assertThat(found).isNotNull();
    assertThat(found.title()).isEqualTo("Find Me");
    assertThat(found.id()).isEqualTo(created.id());
  }

  @Test
  void findByIdShouldReturnNullWhenNoteDoesNotExist() {
    var found = noteRepository.findById("000000000000000000000000");
    assertThat(found).isNull();
  }

  @Test
  void updateShouldModifyExistingNote() {
    var created = noteRepository.create(new NoteData("Original", "Original Content"));
    var updatedData = new NoteData("Updated", "Updated Content");

    var updated = noteRepository.update(created.id().toHexString(), updatedData);

    assertThat(updated).isNotNull();
    assertThat(updated.title()).isEqualTo("Updated");
    assertThat(updated.content()).isEqualTo("Updated Content");
    assertThat(updated.id()).isEqualTo(created.id());
  }

  @Test
  void updateShouldReturnNullWhenNoteDoesNotExist() {
    var updated = noteRepository.update("000000000000000000000000", new NoteData("Title", "Content"));
    assertThat(updated).isNull();
  }

  @Test
  void deleteShouldRemoveNoteAndReturnTrue() {
    var created = noteRepository.create(new NoteData("To Delete", "Content"));

    var deleted = noteRepository.delete(created.id().toHexString());

    assertThat(deleted).isTrue();
    assertThat(noteRepository.findById(created.id().toHexString())).isNull();
  }

  @Test
  void deleteShouldReturnFalseWhenNoteDoesNotExist() {
    var deleted = noteRepository.delete("000000000000000000000000");
    assertThat(deleted).isFalse();
  }
}
