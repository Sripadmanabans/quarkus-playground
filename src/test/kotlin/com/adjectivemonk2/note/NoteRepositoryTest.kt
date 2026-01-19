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
import com.mongodb.client.MongoClient
import com.mongodb.client.model.Filters
import com.varabyte.truthish.assertThat
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import kotlinx.coroutines.test.runTest
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@QuarkusTest
class NoteRepositoryTest {

  @Inject
  lateinit var noteRepository: NoteRepository

  @Inject
  lateinit var mongoClient: MongoClient

  @Inject
  @ConfigProperty(name = "quarkus.mongodb.database") lateinit var databaseName: String

  @AfterEach
  fun closeMongoClient() {
    mongoClient.getDatabase(databaseName).getCollection("notes", Note::class.java).deleteMany(Filters.empty())
  }

  @Test
  fun `create should persist note and return it with an id`() = runTest {
    val data = NoteData(title = "Test Note", content = "Test Content")

    val created = noteRepository.create(data)

    assertThat(created.title).isEqualTo("Test Note")
    assertThat(created.content).isEqualTo("Test Content")
  }

  @Test
  fun `findAll should return all persisted notes`() = runTest {
    noteRepository.create(NoteData(title = "Note 1", content = "Content 1"))
    noteRepository.create(NoteData(title = "Note 2", content = "Content 2"))

    val notes = noteRepository.findAll()

    assertThat(notes.size).isEqualTo(2)
    val titles = notes.map { it.title }.toSet()
    assertThat(titles).containsExactly("Note 1", "Note 2")
  }

  @Test
  fun `findById should return note when it exists`() = runTest {
    val created = noteRepository.create(NoteData(title = "Find Me", content = "Content"))

    val found = noteRepository.findById(created.id.toHexString())

    assertThat(found).isNotNull()
    assertThat(found!!.title).isEqualTo("Find Me")
    assertThat(found.id).isEqualTo(created.id)
  }

  @Test
  fun `findById should return null when note does not exist`() = runTest {
    val found = noteRepository.findById("000000000000000000000000")
    assertThat(found).isNull()
  }

  @Test
  fun `update should modify existing note`() = runTest {
    val created = noteRepository.create(NoteData(title = "Original", content = "Original Content"))
    val updatedData = NoteData(title = "Updated", content = "Updated Content")

    val updated = noteRepository.update(created.id.toHexString(), updatedData)

    assertThat(updated).isNotNull()
    assertThat(updated!!.title).isEqualTo("Updated")
    assertThat(updated.content).isEqualTo("Updated Content")
    assertThat(updated.id).isEqualTo(created.id)
  }

  @Test
  fun `update should return null when note does not exist`() = runTest {
    val updated = noteRepository.update("000000000000000000000000", NoteData("Title", "Content"))
    assertThat(updated).isNull()
  }

  @Test
  fun `delete should remove note and return true`() = runTest {
    val created = noteRepository.create(NoteData(title = "To Delete", content = "Content"))

    val deleted = noteRepository.delete(created.id.toHexString())

    assertThat(deleted).isTrue()
    assertThat(noteRepository.findById(created.id.toHexString())).isNull()
  }

  @Test
  fun `delete should return false when note does not exist`() = runTest {
    val deleted = noteRepository.delete("000000000000000000000000")

    assertThat(deleted).isFalse()
  }
}
