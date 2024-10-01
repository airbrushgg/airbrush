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

### Debugging Airbrush

It is assumed that you're using IntelliJ IDEA for this. If you're not, you will need to look up instructions for your
particular IDE.

1 - Open the `Edit Configurations...` menu in the top right of the IDE.

2 - Click the `+` icon in the top left and select `Remote JVM Debug`. Make sure the debugger mode is set to
`Attach to remote JVM`. The host must be `localhost` and the port must be set to `5005`. When done, click `Apply` and
then `OK`.

3 - Create a breakpoint, then run the `Remote JVM Debug` configuration. This will attach the debugger to the running
JVM instance in the docker container. See [Running Airbrush](#running-airbrush) for how to start Airbrush.