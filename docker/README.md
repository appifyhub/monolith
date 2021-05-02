## Docker Compose support

This project can run in the Docker Compose framework. 
The basic `Dockerfile` is possible from the project's root directory.

To run the service using Docker Compose, you need a `docker-compose.yml` file for each configuration.
Once in the appropriate directory (see the directories below), it's simple to spin it up:

```shell
docker-compose up
```

When you don't need the service anymore, you can just tear it down:

```shell
docker-compose down
```

### How does the local Docker build work?

If there's a build already available (`$projectRoot/build/libs/monolith.jar`), 
the `Dockerfile` build configuration will use it.

If there's no build available, the `Dockerfile` build configuration will launch
a new build during the image build (so, before deploying containers).

Either way, no tests will be executed as it's assumed that you're 
running with Docker as the last step of the development process.

Take a look at the root `Dockerfile` for more information.

### Where can I find the actual compose files?

The compose files (`docker-compose.yml`) are located in this directory, 
each in its separate child directory. They're split by the build flavor they launch.

Here's what's possible for each of the child directories.

  - `local/` - This config launches the local codebase with PostgreSQL, including any local changes made.
    Uses the root `Dockerfile` to build the image if not already built.
    
  - `local/h2/` - This is essentially the same as `local/`, with one key difference: instead of PostgreSQL,
    this configuration launches the service with a built-in `H2` database that runs in-memory.
    
  - `qa` - This configuration pulls the `latest_qa` image from 
    [GitHub Packages](https://github.com/appifyhub/monolith/packages/549093?version=latest_qa).
    Nothing is built. To reconfigure it for another version, you can edit the `docker-compose.yml` config locally.
    
  - `beta` - Similarly to `qa`, this configuration pulls the `latest_beta` image from
    [GitHub Packages](https://github.com/appifyhub/monolith/packages/549093?version=latest_beta).
    
  - `prod` - Similarly to `beta` and `qa`, this configuration pulls the `latest` (prod) image from
    [GitHub Packages](https://github.com/appifyhub/monolith/packages/549093).

All configurations should (theoretically) work well with either PostgreSQL or H2, if reconfigured to use H2.
For information on how to change that configuration, see the next section.

### How do I reconfigure the properties?

Each of the mentioned directories contains a `.env` file containing configurable build properties.
For example, `local`'s H2 variant has an additional `.env` file that allows for that H2 switch.

By default, with each higher build quality (`local` -> `qa` -> `beta` -> `prod`), 
you'll see less and less logging. This is also controlled using the `.env` file.

### Word of caution

It goes without saying, but let's say it anyway:

**DO NOT** deploy any of these configurations directly to production or production-like environments.
All of these are for reference only and should not be used when deploying to real users.
