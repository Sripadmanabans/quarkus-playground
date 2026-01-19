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
import jakarta.ws.rs.NotFoundException
import kotlinx.coroutines.test.runTest
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@QuarkusTest
class NoteResourceTest {

  @Inject
  lateinit var noteResource: NoteResource

  @Inject
  lateinit var mongoClient: MongoClient

  @Inject
  @ConfigProperty(name = "quarkus.mongodb.database") lateinit var databaseName: String

  @AfterEach
  fun closeMongoClient() {
    mongoClient.getDatabase(databaseName).getCollection("notes", Note::class.java).deleteMany(Filters.empty())
  }

  @Test
  fun `list should return empty list when no notes exist`() = runTest {
    val result = noteResource.list()

    assertThat(result).isEmpty()
  }

  @Test
  fun `list should return all notes`() = runTest {
    noteResource.create(NoteData(title = "Note 1", content = "Content 1"))
    noteResource.create(NoteData(title = "Note 2", content = "Content 2"))

    val result = noteResource.list()

    assertThat(result.size).isEqualTo(2)
    val titles = result.map { it.title }.toSet()
    assertThat(titles).containsExactly("Note 1", "Note 2")
  }

  @Test
  fun `create should return note response with id`() = runTest {
    val data = NoteData(title = "New Note", content = "New Content")

    val result = noteResource.create(data)

    assertThat(result.title).isEqualTo("New Note")
    assertThat(result.content).isEqualTo("New Content")
  }

  @Test
  fun `get should return note when it exists`() = runTest {
    val created = noteResource.create(NoteData(title = "Get Me", content = "Content"))

    val result = noteResource.get(created.id)

    assertThat(result.id).isEqualTo(created.id)
    assertThat(result.title).isEqualTo("Get Me")
  }

  @Test
  fun `get should throw NotFoundException when note does not exist`() = runTest {
    assertThrows<NotFoundException> {
      noteResource.get("000000000000000000000000")
    }
  }

  @Test
  fun `update should modify note and return updated response`() = runTest {
    val created = noteResource.create(NoteData(title = "Original", content = "Original Content"))
    val updateData = NoteData(title = "Updated", content = "Updated Content")

    val result = noteResource.update(created.id, updateData)

    assertThat(result.id).isEqualTo(created.id)
    assertThat(result.title).isEqualTo("Updated")
    assertThat(result.content).isEqualTo("Updated Content")
  }

  @Test
  fun `update should throw NotFoundException when note does not exist`() = runTest {
    assertThrows<NotFoundException> {
      noteResource.update("000000000000000000000000", NoteData("Title", "Content"))
    }
  }

  @Test
  fun `delete should remove note successfully`() = runTest {
    val created = noteResource.create(NoteData(title = "To Delete", content = "Content"))

    noteResource.delete(created.id)

    assertThrows<NotFoundException> {
      noteResource.get(created.id)
    }
  }

  @Test
  fun `delete should throw NotFoundException when note does not exist`() = runTest {
    assertThrows<NotFoundException> {
      noteResource.delete("000000000000000000000000")
    }
  }
}
