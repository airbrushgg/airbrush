package gg.airbrush.core.commands.history

import gg.airbrush.core.lib.Constants
import gg.airbrush.core.lib.prettify
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.PlayerUtils
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import java.util.*

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// TODO: Move to SDK
fun timestampToRelativeTime(timestamp: Long): String {
    val currentInstant = Instant.now()
    val targetInstant = Instant.ofEpochMilli(timestamp)

    val zoneId = ZoneId.systemDefault()
    val currentDateTime = LocalDateTime.ofInstant(currentInstant, zoneId)
    val targetDateTime = LocalDateTime.ofInstant(targetInstant, zoneId)

    val diff = ChronoUnit.SECONDS.between(targetDateTime, currentDateTime)

    fun formatAgo(diff: Long, secondsInUnit: Long, unit: String): String {
        val time = diff / secondsInUnit
        return "$time ${if (time > 1) unit + "s" else unit} ago"
    }

    return when {
        diff < 60 -> "Just now"
        diff < 3600 -> formatAgo(diff, 60, "minute")
        diff < 86400 -> formatAgo(diff, 3600, "hour")
        diff < 604800 -> formatAgo(diff, 86400, "day")
        else -> formatAgo(diff, 604800, "week")
    }
}

class History : Command("history"), CommandExecutor {
    private val limitArgument = ArgumentType.Integer("limit")
        .setDefaultValue(5)

    init {
        defaultExecutor = this
        addSyntax(this::apply, limitArgument)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val player = sender as Player

        val block = player.getTargetBlockPosition(Constants.EXTENDED_RANGE) ?: return
        val blockPos =  Pos(
            block.blockX().toDouble(),
            block.blockY().toDouble(),
            block.blockZ().toDouble()
        )

        player.sendMessage("<p>Loading pixel data for this pixel...".mm())

        val limit = context.get(limitArgument) ?: 5
        val pixelData = SDK.pixels.getHistoryAt(blockPos, limit)
        if (pixelData.isEmpty()) {
            sender.sendMessage("<error>Could not fetch pixel data for location".mm())
            return
        }

        val msg = mutableListOf<String>()
        // Reverse the pixel data since it is returned in descending order (by timestamp).
        pixelData.asReversed().forEach {
            val time = timestampToRelativeTime(it.timestamp)
            val painter = PlayerUtils.getName(it.player)
            msg.add("<p><s>$painter</s> painted <s>${it.material.prettify()}</s> ($time)")
        }

        player.sendMessage("<p><s>${msg.size}</s> actions have occured here.".mm())
        player.sendMessage(msg.joinToString("\n").mm())
    }
}