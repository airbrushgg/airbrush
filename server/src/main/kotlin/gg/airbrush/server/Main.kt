package gg.airbrush.server

import gg.airbrush.server.commands.Plugins
import gg.airbrush.server.commands.Stop
import gg.airbrush.server.lib.OperatorSender
import gg.airbrush.server.plugins.PluginManager
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.*
import net.minestom.server.extras.MojangAuth
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.timer.TaskSchedule
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.thread

private val queuedCommands = Collections.synchronizedList(mutableListOf<String>())
private val logger = LogManager.getLogger("Server")
lateinit var consoleThread: Thread

val console = OperatorSender()
val server = MinecraftServer.init()
val pluginManager = PluginManager()
private lateinit var defaultInstance: Instance

fun main() {
    registerEvents()
    registerCommands()
    registerVelocity()

    pluginManager.registerPlugins()
    pluginManager.setupPlugins()

    Runtime.getRuntime().addShutdownHook(Thread {
        pluginManager.teardownPlugins()
    })

    val port = System.getenv("SERVER_PORT") ?: "25565"

    server.start("0.0.0.0", port.toInt())
    consoleThread = setupConsole()

    logger.info("Server started on port $port")
}

fun registerVelocity() {
    val secret = File("velocity.secret")

    if (secret.exists()) {
        VelocityProxy.enable(secret.readText())
        logger.info("Registered Velocity Proxy")
        return
    }

    logger.warn("Velocity proxy could not be enabled! If this is in a dev env, ignore this.")
    MojangAuth.init()
}

fun registerCommands() {
    val manager = MinecraftServer.getCommandManager()

    // todo: improve command manager
    manager.register(Plugins())
    manager.register(Stop())
}

fun setupConsole(): Thread {
    return thread {
        val reader = BufferedReader(InputStreamReader(System.`in`, StandardCharsets.UTF_8))

        while (MinecraftServer.isStarted()) {
            val command = reader.readLine() ?: continue
            queuedCommands.add(command)
        }
    }
}

fun registerEvents() {
    val eventHandler = MinecraftServer.getGlobalEventHandler()
    val commandManager = MinecraftServer.getCommandManager()
    val scheduler = MinecraftServer.getSchedulerManager()

    eventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        event.spawningInstance = defaultInstance
        logger.info("${player.username} joined Airbrush")
    }

    eventHandler.addListener(PlayerDisconnectEvent::class.java) { event ->
        val player = event.player
        logger.info("${player.username} left Airbrush")
    }

    eventHandler.addListener(PlayerSpawnEvent::class.java) { event ->
        val player = event.player
        player.teleport(Pos(0.0, 10.0, 0.0))
    }

    eventHandler.addListener(PlayerCommandEvent::class.java) { event ->
        val player = event.player
        logger.info("${player.username} executed /${event.command}")
    }

    eventHandler.addListener(PlayerChatEvent::class.java) { event ->
        val player = event.player
        logger.info("${player.username}: ${event.message}")
    }

    scheduler.scheduleTask({
        while (queuedCommands.isNotEmpty()) {
            commandManager.execute(console, queuedCommands[0])
            queuedCommands.removeAt(0)
        }
    }, TaskSchedule.immediate(), TaskSchedule.tick(1))
}

fun registerDefaultInstance(instance: Instance) {
    defaultInstance = instance
}