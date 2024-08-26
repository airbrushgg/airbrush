package gg.airbrush.permissions.commands.arguments

import gg.airbrush.permissions.lib.OfflinePlayer
import kotlinx.coroutines.Deferred
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.utils.binary.BinaryWriter
import net.minestom.server.utils.mojang.MojangUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.UUID

private val USERNAME_PATTERN = Regex("\\.?[a-zA-Z0-9_]{1,16}")
private val UUID_PATTERN = Regex("[0-9a-f]{8}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{12}")

class OfflinePlayerArgument(id: String) : Argument<Deferred<OfflinePlayer>>(id) {
    init {
        setSuggestionCallback { _, context, suggestions ->
            val name = context.getRaw(id)

            for (player in MinecraftServer.getConnectionManager().onlinePlayers)
                if (name in player.username)
                    suggestions.addEntry(SuggestionEntry(player.username))
        }
    }

    override fun parse(sender: CommandSender, input: String): Deferred<OfflinePlayer> = runBlocking {
        val isUsername = USERNAME_PATTERN.matches(input)
        val isUUID = UUID_PATTERN.matches(input)

        if (!isUsername && !isUUID)
            throw ArgumentSyntaxException("Invalid player name / uuid.", input, 1)

        async {
            val data = when (isUsername) {
                true -> MojangUtils.fromUsername(input)
                false -> MojangUtils.fromUuid(input)
            } ?: throw ArgumentSyntaxException("That player does not exist.", input, 2)

            val uniqueId = parseUUIDWithoutHyphens(data.get("id").asString)
            val username = data.get("name").asString

            OfflinePlayer(uniqueId, username)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun parseUUIDWithoutHyphens(id: String): UUID {
        val long1 = id.substring(0, 16).hexToLong()
        val long2 = id.substring(16).hexToLong()

        return UUID(long1, long2)
    }

    override fun parser(): String {
        return "brigadier:string"
    }

    override fun nodeProperties(): ByteArray? {
        return BinaryWriter.makeArray { writer -> writer.writeVarInt(0) }
    }
}