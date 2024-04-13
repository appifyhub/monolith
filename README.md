![Codacy grade](https://img.shields.io/codacy/grade/631f1219784b4b068469dcb4a1950aec?logo=codacy&label=Quality&color=51C92A)
![Codacy coverage](https://img.shields.io/codacy/coverage/631f1219784b4b068469dcb4a1950aec?logo=codacy&label=Coverage&color=51C92A)

# Monolith · The AppifyHub's Backbone

## About the project

This repository contains the **complete** codebase of the [AppifyHub](https://appifyhub.com)'s monolith backend service.

The service covers for the majority of everyday, user-facing features, such as: authentication, user management, email and messaging, access management, and other.  
See the rest of this document for a developer's overview and information on how to use it yourself.

### Access points

This service runs as an API in two official places:

  - [AppifyHub API](https://api.appifyhub.com)
  - [_Staging_ AppifyHub API](http://staging.api.appifyhub.com) (no SSL)

The related API docs are located nearby too:

  - [API Docs](https://api.appifyhub.com/docs/index.html)
  - [_Staging_ API Docs](http://staging.api.appifyhub.com/docs/index.html) (no SSL)

The OpenAPI YAMLs are in the same place:

  - [OpenAPI YAML for **Creators**](https://api.appifyhub.com/docs/open-api/creator.yaml)
  - [OpenAPI YAML for **Consumers**](https://api.appifyhub.com/docs/open-api/consumer.yaml)
  - [_Staging_ OpenAPI YAML for **Creators**](http://staging.api.appifyhub.com/docs/open-api/creator.yaml) (no SSL)
  - [_Staging_ OpenAPI YAML for **Consumers**](http://staging.api.appifyhub.com/docs/open-api/consumer.yaml) (no SSL)

### AppifyHub SDK

> A simple Python SDK is generated using the [OpenAPI Generator](https://openapi-generator.tech).

SDKs for other languages can also be generated easily using the same method. Feel free to explore the [PR pipeline](./.github/workflows/qa.yml) in GitHub Actions for more details on how the SDK is generated.

You can find the SDK and its documentation here:

  - [Python SDK](./sdk)

## ⚠️ Before you continue…

If you plan on contributing to this project in any way, please read and acknowledge the [Contributing guide](./CONTRIBUTING) first.

Please also take note of the [License](./LICENSE) and check the [Project Website](https://appifyhub.com) for general information.

## Developer's Overview

Because the complete codebase is open-source, you can inspect and run the monolith service yourself.

### Tech Stack

The project currently uses the following tech stack:

  - Runtime: **Java Virtual Machine** (JVM)
  - Language: **Kotlin**
  - Framework: **Spring Boot**
  - Persistence: **PostgreSQL** (production), **H2** (local)
  - Build System: **Gradle**
  - Continuous Integration: **GitHub Actions**
  - Continuous Deployment: **Argo**
  - Distribution: **Docker** image (mostly managed with **Kubernetes**)

### Building

> The easiest way to run the service is by opening the project using [IntelliJ IDEA](https://www.jetbrains.com/idea) and clicking **Run**.

> There are two main Run Configurations included and appearing in your IDE's Run dropdown:
> 
>  - one that attaches the service to PostgreSQL and expects PGSQL to be available in `localhost`; and
>  - the other that attaches the service to an in-memory H2 database, with no external dependencies.

To **build** and **test** from the _command line_, execute the following instruction from the project's root directory:

```console
$ ./gradlew build
```

To only build (without running tests), execute:

```console
$ ./gradlew assemble
```

### Running the checks

> The recommended way of keeping up to date with project styles is to install the **KtLint plugin** for your IntelliJ IDEA. Search for it in **IntelliJ IDEA** (main menu) > **Preferences** / **Settings** > **Plugins**. KtLint's configuration is stored in `.editorconfig`, and the plugin configures itself from there automatically. After installing the plugin, simply format your code using your IDE's formatter shortcut.

> In addition to style checks, there's a runner configuration for IntelliJ IDEA included in the project. You should see a Run Configuration in your IDE called "Run all tests" (or similar). Clicking on it will perform the full check on your local machine.

To run style checks and tests from the command line, execute:

```console
$ ./gradlew check
```

To run only tests (without style checks), execute:

```console
$ ./gradlew test
```

To run only style checks (without tests), execute:

```console
$ ./gradlew ktlintCheck
```

To run the command line formatter, execute:

```console
$ ./gradlew ktlintFormat
```

### Running the service

> The service comes with a built-in server packaged into the JAR executable, and no additional archive deployment is needed. The configuration for launching the service has sensible defaults and you should be able to simply run it from your IDE.

> Additional service configuration is managed through the application config files and environment variables – see `src/main/resources/application.yml` to understand the details.

To run the service from the command line, execute:

```console
$ ./gradlew bootRun
```

#### Docker support

This project is also available as a **Docker** image.  
For more information on how to run it from Docker, see the [docker](./docker) directory.

### Reference Documentation

Top-level Spring reference:

  - [Official Gradle documentation](https://docs.gradle.org)
  - [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.4.1/gradle-plugin/reference/html)
  - [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.4.1/gradle-plugin/reference/html/#build-image)
  - [Spring Security](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-security)
  - [Spring Web](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-developing-web-applications)
  - [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-jpa-and-spring-data)
  - [JDBC API](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-sql)
  - [Spring Data JDBC](https://docs.spring.io/spring-data/jdbc/docs/current/reference/html)
  - [Spring Web Services](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-webservices)

Guides on how to get the basic stuff done in Spring Boot:

- [Securing a Web Application](https://spring.io/guides/gs/securing-web)
- [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2)
- [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap)
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service)
- [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content)
- [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa)
- [Accessing Relational Data using JDBC with Spring](https://spring.io/guides/gs/relational-data-access)
- [Managing Transactions](https://spring.io/guides/gs/managing-transactions)
- [Using Spring Data JDBC](https://github.com/spring-projects/spring-data-examples/tree/master/jdbc/basics)
- [Producing a SOAP web service](https://spring.io/guides/gs/producing-web-service)
- [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
