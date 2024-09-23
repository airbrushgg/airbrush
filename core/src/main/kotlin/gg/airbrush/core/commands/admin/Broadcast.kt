package gg.airbrush.core.commands.admin

import gg.airbrush.discord.bot
import gg.airbrush.discord.discordConfig
import gg.airbrush.server.lib.mm
import gg.airbrush.server.server
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Broadcast : Command("broadcast"), CommandExecutor {
    private val message = ArgumentType.String("message")

    init {
        defaultExecutor = this

        setCondition { sender, _ -> sender.hasPermission("admin.broadcast") }
        addSyntax(this, message)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val message = "<p>Airbrush</p> <g>-</g> <s>${context.get(message)}".mm()
        Audiences.all().sendMessage(message)

        val timestamp = DateTimeFormatter.ofPattern("HH:mm:ss z").format(ZonedDateTime.now())
        val chatChannel = bot.getTextChannelById(discordConfig.channels.main)
            ?: return

        val plainMessage = PlainTextComponentSerializer.plainText().serialize(message)
            .replace("`", "'")
        chatChannel.sendMessage("`[$timestamp] $plainMessage`").queue()
    }
}