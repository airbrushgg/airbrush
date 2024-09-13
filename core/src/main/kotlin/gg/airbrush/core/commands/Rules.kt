

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
    // Overview
    """
        <b>1.</b> Be respectful of one another
        <b>2.</b> No inappropriate content
        <b>3.</b> Threats will result in a permanent ban
        <b>4.</b> No advertisements
        <b>5.</b> Griefing + trolling are not welcome
        
        <#19911d>For further detail on each rule, continue reading >
    """.trimIndent(),

    // Rule 1
    """
        <b>1. Be respectful of one another</b>
        Discrimination in any form is not tolerated anywhere on Airbrush.
        Being disrespectful to others, instigating arguments, attacking
        people especially for no reason are also not welcome.
    """.trimIndent(),

    // Rule 2
    """
        <b>2. No inappropriate content</b>
        NFSW topics or themes are not allowed in neither chat nor paint.
        Politics are discouraged as it is a near guarantee of turning hostile.
    """.trimIndent(),

    // Rule 3
    """
        <b>3. Threats will result in a permanent ban</b>
        Do you really need an explanation for this?
    """.trimIndent(),

    // Rule 4
    """
        <b>4. No advertisements</b>
        Please do not advertise other servers, Discord servers, websites, etc.
    """.trimIndent(),

    // Rule 5
    """
        <b>5. Griefing + trolling are not welcome</b>
        If someone is painting something, try to not disturb and paint elsewhere.
        If you are only here to troll, harass, needlessly grief, etc. there is no reason
        for your presence.
    """.trimIndent()
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
            book.addPage("$airbrushTitle\n${pages[page]}".mm())
        } else {
            pages.forEach { content -> book.addPage("$airbrushTitle\n$content".mm()) }
        }

        sender.openBook(book)
    }
}