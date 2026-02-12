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
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class NoteRepository {

  private final MongoClient mongoClient;
  private final String databaseName;

  public NoteRepository(
      MongoClient mongoClient,
      @ConfigProperty(name = "quarkus.mongodb.database") String databaseName) {
    this.mongoClient = mongoClient;
    this.databaseName = databaseName;
  }

  private MongoCollection<Note> getCollection() {
    return mongoClient.getDatabase(databaseName).getCollection("notes", Note.class);
  }

  public List<Note> findAll() {
    return getCollection().find().into(new ArrayList<>());
  }

  public Note findById(String id) {
    return getCollection().find(Filters.eq("_id", new ObjectId(id))).first();
  }

  public Note create(NoteData data) {
    var note = Note.of(new ObjectId(), data);
    getCollection().insertOne(note);
    return note;
  }

  public Note update(String id, NoteData data) {
    var objectId = new ObjectId(id);
    var note = Note.of(objectId, data);
    var result = getCollection().replaceOne(Filters.eq("_id", objectId), note);
    return result.getModifiedCount() > 0 ? note : null;
  }

  public boolean delete(String id) {
    var result = getCollection().deleteOne(Filters.eq("_id", new ObjectId(id)));
    return result.getDeletedCount() > 0;
  }
}
