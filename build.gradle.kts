import com.github.breadmoirai.githubreleaseplugin.ChangeLogSupplier
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
  val kotlinVersion = "1.4.31"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  kotlin("plugin.jpa") version kotlinVersion
  kotlin("plugin.noarg") version kotlinVersion
  kotlin("plugin.allopen") version kotlinVersion
  kotlin("kapt") version kotlinVersion

  id("org.springframework.boot") version "2.4.1"
  id("io.spring.dependency-management") version "1.0.10.RELEASE"
  id("com.github.breadmoirai.github-release") version "2.2.12"
}

repositories {
  jcenter()
  mavenCentral()
}

dependencies {

  // language essentials
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  // web framework
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-web-services")
  implementation("com.googlecode.libphonenumber:libphonenumber:8.+")

  // auth
  implementation("com.auth0:java-jwt:3+")
  implementation("org.springframework.security:spring-security-core")
  implementation("org.springframework.security:spring-security-web")
  implementation("org.springframework.security:spring-security-config")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  // persistence
  implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-data-rest")
  implementation("org.hibernate:hibernate-core")

  // annotation processors
  implementation("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  kapt("org.springframework.boot:spring-boot-configuration-processor")

  // runtime dependencies
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql")

  // test annotation processors
  testImplementation("org.springframework.boot:spring-boot-configuration-processor")
  testAnnotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  // tests
  testImplementation(platform("org.junit:junit-bom:5+"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.hibernate:hibernate-testing")
  testImplementation("com.h2database:h2")
  testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.+")
  testImplementation("org.mockito:mockito-core:3.+")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2+")

}

// endregion

// region Project configuration

group = prop("group")
version = prop("version")
val artifact = prop("artifact")

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

// endregion

// region Tasks configuration

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

// region Plugin configuration

allOpen {
  annotation("javax.persistence.Entity")
  annotation("javax.persistence.Embeddable")
  annotation("javax.persistence.MappedSuperclass")
}

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

  val maxChanges = 5
  val bullet = "\n* "
  val changelogConfig = closureOf<ChangeLogSupplier> {
    currentCommit("HEAD")
    lastCommit("HEAD~$maxChanges")
    options("--format=oneline", "--abbrev-commit", "--max-count=$maxChanges")
  }
  val changes = try {
    changelog(changelogConfig).call()
      .trim()
      .split("\n")
      .map { it.trim() }
  } catch (t: Throwable) {
    System.err.println("Failed to fetch history")
    t.printStackTrace(System.err)
    emptyList()
  }

  body(
    when {
      changes.isNotEmpty() -> "## Last $maxChanges changes\n$bullet${changes.joinToString(bullet)}"
      else -> "See commit history for latest changes."
    }
  )

  releaseAssets(arrayOf(
    file("${project.buildDir}/libs/$artifact.jar")
  ))
}

apply(plugin = "com.github.breadmoirai.github-release")

// endregion

// region Helpers

object Env {
  const val INVALID = "<invalid>"
  fun get(name: String, default: String = INVALID) =
    System.getenv(name).takeIf { !it.isNullOrBlank() } ?: default
}

fun prop(name: String) = properties[name]?.toString() // from gradle.properties
  ?: error("Missing required '$name' in gradle.properties")

// endregion