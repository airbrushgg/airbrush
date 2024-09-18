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

package gg.airbrush.core.commands

import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.kyori.adventure.inventory.Book
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

private val pages: List<String> = listOf(
    Translations.translate("core.commands.rules.overview").trimIndent(),
    Translations.translate("core.commands.rules.rule1").trimIndent(),
    Translations.translate("core.commands.rules.rule2").trimIndent(),
    Translations.translate("core.commands.rules.rule3").trimIndent(),
    Translations.translate("core.commands.rules.rule4").trimIndent(),
    Translations.translate("core.commands.rules.rule5").trimIndent()
)

class Rules : Command("rules"), CommandExecutor {
    private val pageNumber = ArgumentType.Integer("page")
    private val airbrushTitle = Translations.translate("core.scoreboard.title") + " <b><#712D73>Rules</#712D73></b>"

    init {
        defaultExecutor = this
        addSyntax(this, pageNumber)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player) return

        val book = Book.builder()

        if (context.has(pageNumber)) {
            var page = context.get(pageNumber)
            if (page < 0 || page > pages.size) {
                page = 0
            }
            book.addPage("$airbrushTitle\n\n${pages[page]}".mm())
        } else {
            pages.forEach { content -> book.addPage("$airbrushTitle\n\n$content".mm()) }
        }

        sender.openBook(book)
    }
}