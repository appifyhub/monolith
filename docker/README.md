## Docker Compose support

This project can run in the Docker Compose framework. 

The basic `Dockerfile` is available in the project's root directory. To run the service using Docker Compose, you need a `docker-compose.yml` file for each flavor – configured in separate directories under here.

The basic command to run the configured image is:

```console
$ docker-compose up
```

When you don't need the service anymore, you can just tear it down:

```console
$ docker-compose down
```

### How does the local Docker build work?

If there's a build already available (`$projectRoot/build/libs/monolith.jar`), the `Dockerfile` script will use it.

If there's no build available, the `Dockerfile` build configuration will start a new build during the image assembly (and before deploying containers).

Docker is not a development environment, therefore no tests will be executed during Docker builds. It's assumed that you're running Docker as the last step of your development process.

### Where can I find the Docker Compose files?

The compose files (`docker-compose.yml`) are located in this directory, each in its separate child directory. They're named appropriately following the build flavor they launch.

Here's what's possible for each of the child directories.

  - `local/` - This config launches the local codebase attached to a local PostgreSQL, including any local changes made. Uses the root `Dockerfile` to build the image (if not already built). You can force a rebuild by running `./gradlew clean` first.
    
  - `local/h2/` - This is essentially the same as `local/`, with one key difference: instead of PostgreSQL, this configuration launches the service with a built-in `H2` database that runs in-memory.
    
  - `qa` - This configuration pulls the `latest_pr` image from [Docker Hub](https://hub.docker.com/r/appifyhub/service/tags?page=1&ordering=last_updated&name=latest_pr). Nothing is built. To reconfigure it for another version, you can edit the `docker-compose.yml` file locally.
    
  - `beta` - Similarly to `qa`, this configuration pulls the `latest_beta` image from [Docker Hub](https://hub.docker.com/r/appifyhub/service/tags?page=1&ordering=last_updated&name=latest_beta).
    
  - `prod` - Similarly to `beta` and `qa`, this configuration pulls the `latest` (production) image from [Docker Hub](https://hub.docker.com/r/appifyhub/service/tags?page=1&ordering=last_updated&name=latest).

All flavors should work well with both PostgreSQL and H2. For information on how to change that configuration, see the next section.

### How do I reconfigure the project properties?

Each of the mentioned directories contains a `.env` file containing configurable build properties.  
For example, `local`'s H2 variant has an additional `.env` file that allows for that H2 switch.

By default, with each higher build quality (`local` -> `qa` -> `beta` -> `prod`), you'll see less and less logging.  
This is also controlled using the `.env` files.

### Word of caution

It goes without saying, but let's say it anyway:

**DO NOT** deploy any of these configurations directly to production or production-like environments.  
All of these are for testing only and should not be used when deploying to real environments and real users.
