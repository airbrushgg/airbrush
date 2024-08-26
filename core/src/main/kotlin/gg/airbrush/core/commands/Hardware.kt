package gg.airbrush.core.commands

import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor

class Hardware : Command("hardware", "hw"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val runtime = Runtime.getRuntime()

        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
        val maxHeapSize = runtime.maxMemory() / 1048576L
        val availableHeapSize = maxHeapSize - usedMemory

        // todo: add tps averages and cpu usage
        sender.sendMessage("""<s>
            |Memory: <p>${usedMemory}mb</p>/<p>${maxHeapSize}mb</p> (<p>${availableHeapSize}mb</p> available)
        """.trimMargin().mm())
    }
}