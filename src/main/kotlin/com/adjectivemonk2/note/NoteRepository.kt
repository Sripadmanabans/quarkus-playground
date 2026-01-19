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
import com.mongodb.client.model.Filters
import io.quarkus.mongodb.reactive.ReactiveMongoClient
import io.quarkus.mongodb.reactive.ReactiveMongoCollection
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.bson.types.ObjectId
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class NoteRepository(
  private val mongoClient: ReactiveMongoClient,
  @param:ConfigProperty(name = "quarkus.mongodb.database") private val databaseName: String,
) {

  private fun getCollection(): ReactiveMongoCollection<Note> =
    mongoClient.getDatabase(databaseName).getCollection("notes", Note::class.java)

  suspend fun findAll(): List<Note> =
    getCollection().find().collect().asList().awaitSuspending()

  suspend fun findById(id: String): Note? =
    getCollection().find(Filters.eq("_id", ObjectId(id))).collect().first().awaitSuspending()

  suspend fun create(data: NoteData): Note {
    val note = Note(ObjectId(), data)
    getCollection().insertOne(note).awaitSuspending()
    return note
  }

  suspend fun update(id: String, data: NoteData): Note? {
    val objectId = ObjectId(id)
    val note = Note(objectId, data)
    val result = getCollection()
      .replaceOne(Filters.eq("_id", objectId), note)
      .awaitSuspending()
    return if (result.modifiedCount > 0) note else null
  }

  suspend fun delete(id: String): Boolean {
    val result = getCollection()
      .deleteOne(Filters.eq("_id", ObjectId(id)))
      .awaitSuspending()
    return result.deletedCount > 0
  }
}
