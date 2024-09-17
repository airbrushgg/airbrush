# Airbrush Dev Environment

This is a collection of tools and files to help you get started with contributing to Airbrush.

> [!IMPORTANT]
> It is advised that you're familiar with the basics of [Kotlin](https://kotlinlang.org) before continuing. Knowledge with [Minestom](https://minestom.net/) is also a plus!

## Prerequisites

- Docker Desktop (https://www.docker.com/products/docker-desktop)
- (that's literally it)

## Getting Started

### Setting up your jars

1 - Clone the Airbrush repo:

```bash
git clone https://github.com/airbrushgg/airbrush.git
```
2 - Open it in your favorite Kotlin IDE (we use IntelliJ IDEA). You can also use the `gradlew` wrapper to build the project:

```bash
./gradlew shadowJar
```
Please be sure to execute this within the root directory, and not a submodule. Once the build is complete, you'll have all the necessary files within this `dev-env` directory.

### Running Airbrush

In order to run Airbrush, simply execute:

```bash
docker compose up -d
```

This will start up a local instance of MongoDB for you to use (preconfigured in the auto-generated configuration), along with the Airbrush server itself. 

When running for the first time, you'll see some errors from our Discord plugin, example:

```
java.lang.IllegalArgumentException: Token may not be empty
```

It is important to fix this by going into `/dev-env/plugins/discord/config.toml` and filling in the blank values respectively. Then, restart the container.

Once Airbrush is up and running, you're free to join with the IP of `127.0.0.1:25565`. Files within `/plugins` are automatically updated within the container, along with the base `airbrush.jar`.

> [!NOTE]
> When updating a singular plugin, instead of running `./gradlew shadowJar` in the root, you can run it on the particular submodule itself. This way only that particular plugin is updated within the development container, *and* you save some time!

