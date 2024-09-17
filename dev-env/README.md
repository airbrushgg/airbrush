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

### Filling out your configuration files

We've tried to give everyone a decent starting point, so most configuration files are filled out for you, using the values we use ourselves. However, some files contain sensitive information, you'll have to fill out these yourself.

### [`/plugins/discord/config.example.toml`](/dev-env/plugins/discord/config.example.toml) 

This file includes information for our Discord plugin, it manages our Discord bridge and provides an API for our other plugins to use.<br>

### [`/plugins/sdk/config.example.toml`](/dev-env/plugins/sdk/config.example.toml) 

This file includes crucial information for our SDK, the central piece of Airbrush. Everything here is ready for use right out of the box, but the main file is hidden just in case you want to use a different MongoDB database.

### Running Airbrush

In order to run Airbrush, simply execute:

```bash
docker compose up -d
```

This will start up a local instance of MongoDB for you to use (preconfigured in [`/plugins/sdk/config.example.toml`](/dev-env/plugins/sdk/config.example.toml)), along with the Airbrush server itself.

> [!TIP]
> It's best to use Docker Desktop when developing, as it allows you to easily manage the development containers (live logs, restarting, deleting, etc.)

Once Airbrush is up and running, you're free to join with the IP of `127.0.0.1:25565`. Files within [`/plugins`](/dev-env/plugins/sdk/config.example.toml) are automatically updated within the container, along with the base `airbrush.jar`.

> [!NOTE]
> When updating a singular plugin, instead of running `./gradlew shadowJar` in the root, you can run it on the particular submodule itself. This way only that particular plugin is updated within the development container, *and* you save some time!

