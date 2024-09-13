/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2023 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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