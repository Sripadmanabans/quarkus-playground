package com.adjectivemonk2.increment

import io.quarkus.redis.datasource.ReactiveRedisDataSource
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class IncrementRepository @Inject constructor(dataSource: ReactiveRedisDataSource) {
  private val commands = dataSource.value(String::class.java, Long::class.java)
  private val keyCommands = dataSource.key()

  suspend fun get(id: String): Long {
    return commands[id].awaitSuspending() ?: 0L
  }

  suspend fun set(id: String, value: Long) {
    commands.set(id, value).awaitSuspending()
  }

  suspend fun increment(id: String, incrementBy: Long) {
    commands.incrby(id, incrementBy).awaitSuspending()
  }

  suspend fun delete(id: String) {
    keyCommands.del(id).awaitSuspending()
  }

  suspend fun keys(): List<String> {
    return keyCommands.keys("*").awaitSuspending()
  }
}
