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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.allopen)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.quarkus)
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  implementation(enforcedPlatform(libs.quarkus.bom))
  implementation(libs.quarkus.kotlin)
  implementation(libs.quarkus.rest.kotlin.serialization)
  implementation(libs.quarkus.arc)
  implementation(libs.quarkus.rest)
  implementation(libs.quarkus.config.yaml)
  implementation(libs.quarkus.mongodb.client)
  implementation(libs.quarkus.redis)
  implementation(libs.coroutines.core)

  implementation(platform(libs.mongo.bom))
  implementation(libs.mongo.driver)

  testImplementation(libs.quarkus.junit5)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.rest.assured)
  testImplementation(libs.truthish)
}

group = "com.adjectivemonk2"
version = "1.0.0-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<Test> {
  systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

allOpen {
  annotation("jakarta.ws.rs.Path")
  annotation("jakarta.enterprise.context.ApplicationScoped")
  annotation("jakarta.persistence.Entity")
  annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_25
    javaParameters = true
  }
}
