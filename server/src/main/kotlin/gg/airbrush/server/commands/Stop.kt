package gg.airbrush.server.commands

import gg.airbrush.server.consoleThread
import gg.airbrush.server.lib.mm
import gg.airbrush.server.server
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class Stop : Command("stop"), CommandExecutor {
    init {
        setCondition { sender, _ ->
            sender.hasPermission("airbrush.stop")
        }

        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        for (player in MinecraftServer.getConnectionManager().onlinePlayers)
            player.kick("<s>Server is stopping.".mm())

	    val manager = MinecraftServer.getInstanceManager()
	    manager.instances.forEach {
			it.saveChunksToStorage()
	    }

        thread {
            Thread.sleep(1000)
            MinecraftServer.stopCleanly()
            consoleThread.interrupt()
            Thread.sleep(500)
            exitProcess(0)
        }
    }
}