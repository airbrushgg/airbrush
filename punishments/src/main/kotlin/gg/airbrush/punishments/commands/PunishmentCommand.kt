/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2024 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.punishments.commands

import gg.airbrush.punishments.enums.PunishmentTypes
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.*
import gg.airbrush.server.lib.mm
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import java.util.UUID

class PunishmentCommand : Command("punishment") {
    private val idArg = ArgumentType.String("id")
    init {
        setCondition { sender, _ ->
            sender.hasPermission("core.staff")
        }

        defaultExecutor = CommandExecutor { sender, _ ->
            sender.sendMessage("<error>Invalid usage.".mm())
        }

        addSyntax(this::apply, idArg)
    }

    private fun apply(sender: CommandSender, context: CommandContext) = runBlocking {
        val punishmentId = UUID.fromString(context.get("id"))

        if(!SDK.punishments.exists(punishmentId)) {
            sender.sendMessage("<error>Invalid punishment ID.".mm())
            return@runBlocking
        }

        val punishment = SDK.punishments.get(punishmentId)

        val pages = mutableListOf<Component>()
        val book = Book.builder().author(Component.empty()).title(Component.empty())

        val playerName = PlayerUtils.getName(punishment.getPlayer())
        val moderatorName = PlayerUtils.getName(punishment.getModerator())
        val punishmentType = PunishmentTypes.entries[punishment.data.type]

        val placeholders = listOf(
            Placeholder("%player%", playerName),
            Placeholder("%moderator%", moderatorName),
            Placeholder("%reason%", punishment.data.reason),
            Placeholder("%type%", punishmentType.name)
        )

        val translation = Translations.getString("punishments.view.overview").parsePlaceholders(placeholders)
        val page = translation.trimIndent().replaceTabs()

        println(page)

        pages.add(page.mm())

        sender.openBook(book.pages(pages))
    }
}