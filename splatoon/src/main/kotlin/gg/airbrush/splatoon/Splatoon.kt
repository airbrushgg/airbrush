package gg.airbrush.splatoon

import gg.airbrush.server.plugins.Plugin
import gg.airbrush.splatoon.commands.SplatoonCommand
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import org.slf4j.LoggerFactory

class Splatoon : Plugin() {

    private val commandManager = MinecraftServer.getCommandManager()
    private val commands = listOf<Command>(
        SplatoonCommand()
    )

    override fun setup() {
        commands.forEach { commandManager.register(it) }
    }

    override fun teardown() {
        commands.forEach { commandManager.unregister(it) }
    }

    companion object {
        internal val logger = LoggerFactory.getLogger(Splatoon::class.java)
    }
}