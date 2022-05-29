import com.github.breadmoirai.githubreleaseplugin.ChangeLogSupplier
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

// region Build script configuration

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("joda-time:joda-time:+")
    classpath("org.jlleitschuh.gradle:ktlint-gradle:10.+")
  }
}

plugins {
  val kotlinVersion = "1.6.21"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  kotlin("plugin.jpa") version kotlinVersion
  kotlin("plugin.noarg") version kotlinVersion
  kotlin("plugin.allopen") version kotlinVersion
  kotlin("kapt") version kotlinVersion

  id("org.springframework.boot") version "2.6.7"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("com.github.breadmoirai.github-release") version "2.2.12"
}

repositories {
  mavenCentral()
}

@Suppress("GradlePackageUpdate")
dependencies {

  // language essentials
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  // web frameworks
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-web-services")
  implementation("org.apache.httpcomponents:httpclient")

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

  // helpers
  implementation("com.ip2location:ip2location-java:8.+")
  implementation("com.googlecode.libphonenumber:libphonenumber:8.+")

  // annotation processors
  kapt("org.springframework.boot:spring-boot-configuration-processor")
  implementation("org.springframework.boot:spring-boot-configuration-processor")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

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
  testImplementation("org.mockito:mockito-core:4.4.+")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.+")

  // as per https://spring.io/blog/2021/12/10/log4j2-vulnerability-and-spring-boot
  extra["slf4j.version"] = "2.+"
}

// endregion

// region Project configuration

group = prop("group")
version = prop("version")
val artifact = prop("artifact")
val packageName = "$group.$artifact"

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = java.sourceCompatibility

// endregion

// region Tasks configuration

tasks {

  register<SourceTask>("propertyGenerator") {
    setSource(file("gradle.properties"))

    doLast {
      file("src/main/resources/generated.properties")
        .apply { createNewFile() }
        .writeText(
          """
            # This is a generated file. Your changes won't be saved.
            # Created at: ${org.joda.time.LocalDateTime.now()}
            # suppress inspection "UnusedProperty" for whole file

            version=$version
            quality=${Env.get("BUILD_QUALITY", default = "Debug").toLowerCase()}
          """.trimIndent(),
          Charsets.UTF_8
        )
    }
  }

  bootJar {
    mainClass.set("$packageName.AppifyHubApplicationKt")
    archiveFileName.set("$artifact.jar")
  }

  withType<KotlinCompile>().all {
    dependsOn("propertyGenerator")

    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = java.sourceCompatibility.majorVersion
    }
  }

  withType<Test> {
    useJUnitPlatform()

    with(testLogging) {
      showStandardStreams = true
      exceptionFormat = TestExceptionFormat.FULL
      events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }

    minHeapSize = "512m"
    maxHeapSize = "1024m"

    // as per https://stackoverflow.com/a/39753210/2102748
    val desiredForks = Runtime.getRuntime()?.availableProcessors()?.div(2) ?: 1
    maxParallelForks = desiredForks.coerceAtLeast(1)
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

  val maxFetched = 20
  val maxReported = 7
  val bullet = "\n* "
  val changelogConfig = closureOf<ChangeLogSupplier> {
    currentCommit("HEAD")
    lastCommit("HEAD~$maxFetched")
    options("--format=oneline", "--abbrev-commit", "--max-count=$maxFetched")
  }
  val ignoredMessagesRegex = setOf(
    "(?i).*bump.*version.*",
    "(?i).*increase.*version.*",
    "(?i).*version.*bump.*",
    "(?i).*version.*increase.*",
    "(?i).*merge.*request.*",
    "(?i).*request.*merge.*",
  ).map(String::toRegex)

  val changes = try {
    changelog(changelogConfig)
      .call()
      .trim()
      .split("\n")
      .map { it.trim() }
      .filterNot { ignoredMessagesRegex.any(it::matches) }
      .take(maxReported)
  } catch (t: Throwable) {
    System.err.println("Failed to fetch history")
    t.printStackTrace(System.err)
    emptyList()
  }

  body(
    when {
      changes.isNotEmpty() -> "## Latest changes\n${changes.joinToString(separator = bullet, prefix = bullet)}"
      else -> "See commit history for latest changes."
    }
  )

  releaseAssets(
    arrayOf(
      file("${project.buildDir}/libs/$artifact.jar")
    )
  )
}
apply(plugin = "com.github.breadmoirai.github-release")

apply(plugin = "org.jlleitschuh.gradle.ktlint")
configure<KtlintExtension> {
  verbose.set(true)
  // also update in .editorconfig
  disabledRules.set(setOf("import-ordering", "no-blank-line-before-rbrace"))
}

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
