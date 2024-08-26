package gg.airbrush.permissions.commands.arguments

import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.ranks.AirbrushRank
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.utils.binary.BinaryWriter

val rankCache = SDK.ranks.list().toMutableList()

class RankArgument(id: String) : Argument<AirbrushRank>(id) {
    init {
        setSuggestionCallback { _, context, suggestions ->
            val name = context.getRaw(id)

            for (rank in rankCache.map { it.getData().name }.filter { name in it })
                suggestions.addEntry(SuggestionEntry(rank))
        }
    }

    override fun parse(sender: CommandSender, input: String): AirbrushRank {
        if (!SDK.ranks.exists(input))
            throw ArgumentSyntaxException("Rank not found", input, 1)

        return SDK.ranks.get(input)
    }

    override fun parser(): String {
        return "brigadier:string"
    }

    override fun nodeProperties(): ByteArray? {
        return BinaryWriter.makeArray { writer -> writer.writeVarInt(0) }
    }
}