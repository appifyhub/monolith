# Contributing

Feel free to open pull requests, start discussions, report bugs and suggest new features.

Be open to suggestions, constructive criticism and be tolerant towards everyone involved. 

## The building blocks

### Tech stack

  - Language: **Kotlin** (v1.4+)
  - Environment: **JVM** (best with `JDK 11`)
  - Framework: **Spring Boot** (v2)
  - Storage: **PostgreSQL & H2** (as base)
  - Build System: **Gradle** (v6.7+)
  - CI: **GitHub** Actions
  - Delivery: **Docker** with Compose

This project follows the most common practices for the tech stack chosen.

## Building and testing the project

To build and test, run the following (from the project's root directory):

```bash
./gradlew build
```

To only build (without tests), run:

```bash
./gradlew assemble
```

To only run quality checks and test (this might build), run:

```bash
./gradlew check
```

To run Kotlin lint checks locally, the recommended way is to install the **KtLint plugin** from your IntelliJ IDEA > Preferences > Plugins. KtLint configuration is in `.editorconfig`.
KtLint can be run using Gradle:

```bash
./gradlew ktlintCheck
```

## Reference Documentation

### Top-level Spring reference

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.4.1/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.4.1/gradle-plugin/reference/html/#build-image)
* [Spring Security](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-security)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-developing-web-applications)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-jpa-and-spring-data)
* [JDBC API](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-sql)
* [Spring Data JDBC](https://docs.spring.io/spring-data/jdbc/docs/current/reference/html/)
* [Spring Web Services](https://docs.spring.io/spring-boot/docs/2.4.1/reference/htmlsingle/#boot-features-webservices)

### Guides

Guides on how to get the basic stuff done:

* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Accessing Relational Data using JDBC with Spring](https://spring.io/guides/gs/relational-data-access/)
* [Managing Transactions](https://spring.io/guides/gs/managing-transactions/)
* [Using Spring Data JDBC](https://github.com/spring-projects/spring-data-examples/tree/master/jdbc/basics)
* [Producing a SOAP web service](https://spring.io/guides/gs/producing-web-service/)

### Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)
