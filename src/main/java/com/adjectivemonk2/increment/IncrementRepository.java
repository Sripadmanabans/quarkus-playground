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

package com.adjectivemonk2.increment;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class IncrementRepository {

  private final ValueCommands<String, Long> commands;
  private final KeyCommands<String> keyCommands;

  public IncrementRepository(RedisDataSource dataSource) {
    this.commands = dataSource.value(String.class, Long.class);
    this.keyCommands = dataSource.key(String.class);
  }

  public long get(String id) {
    var value = commands.get(id);
    return value != null ? value : 0L;
  }

  public void set(String id, long value) {
    commands.set(id, value);
  }

  public void increment(String id, long incrementBy) {
    commands.incrby(id, incrementBy);
  }

  public void delete(String id) {
    keyCommands.del(id);
  }

  public List<String> keys() {
    return keyCommands.keys("*");
  }
}
