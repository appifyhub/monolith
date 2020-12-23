import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

// region Build script configuration

buildscript {
  repositories {
    jcenter()
    mavenCentral()
  }
  dependencies {
    classpath("joda-time:joda-time:+")
  }
}

plugins {
  kotlin("jvm") version "1.4.21"
  kotlin("plugin.spring") version "1.4.21"
  kotlin("plugin.jpa") version "1.4.21"

  id("org.springframework.boot") version "2.4.1"
  id("io.spring.dependency-management") version "1.0.10.RELEASE"
  id("com.github.breadmoirai.github-release") version "2.2.12"
}

repositories {
  jcenter()
  mavenCentral()
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-web-services")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")

  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql")
}

// endregion

// region Project configuration

group = prop("group")
version = prop("version")
val artifact = prop("artifact")

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

// endregion

// Tasks configuration

tasks {

  withType<Jar> {
    archiveFileName.set("$artifact.jar")
  }

  withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = "11"
    }
  }

  withType<Test> {
    useJUnitPlatform()
    with(testLogging) {
      showStandardStreams = true
      exceptionFormat = TestExceptionFormat.FULL
      events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
  }

  named("githubRelease") {
    dependsOn("bootJar")
  }

}

// endregion

// Plugin configuration

githubRelease {
  val writeToken = Env.get("GITHUB_TOKEN")
  if (writeToken == Env.INVALID) println("Set 'PACKAGES_TOKEN' environment variable to enable GitHub releases")

  val quality = Env.get("BUILD_QUALITY", default = "Debug")
  println("GitHub release configured for '$quality' quality")

  val commitish = Env.get("GITHUB_SHA", default = "main")

  val tag = when (quality) {
    "Debug" -> {
      val formatter = DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss").withZoneUTC()
      val timestamp = Instant.now().toString(formatter)
      "v$version.${quality.toLowerCase()}.$timestamp"
    }
    else -> "v$version.${quality.toLowerCase()}"
  }

  token(writeToken)
  owner(group.toString().split(".").last())
  repo(artifact)
  tagName(tag)
  releaseName("Service [$quality]: $version")
  targetCommitish(commitish)
  prerelease(quality != "GA")
  body("## Changelog\n* ${changelog().call().trim().split("\n").joinToString("\n* ")}")

  releaseAssets(arrayOf(
    file("${project.buildDir}/libs/$artifact.jar")
  ))
}

apply(plugin = "com.github.breadmoirai.github-release")

// endregion

// Helpers

object Env {
  const val INVALID = "<invalid>"
  fun get(name: String, default: String = INVALID) =
    System.getenv(name).takeIf { !it.isNullOrBlank() } ?: default
}

fun prop(name: String) = properties[name]?.toString() // from gradle.properties
  ?: error("Missing required '$name' in gradle.properties")

// endregion