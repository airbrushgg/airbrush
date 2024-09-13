

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

import gg.airbrush.core.lib.Constants
import gg.airbrush.core.lib.PaletteUtil
import gg.airbrush.core.lib.prettify
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.players.PaletteType
import gg.airbrush.sdk.lib.Translations
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemComponent

class Palette : Command("palette"), CommandExecutor {
    private val palettes = PaletteType.entries.map {
        it.name.prettify()
    }.toTypedArray()

    private val chosenPalette = ArgumentType.Word("palette")
        .from(*palettes)

    init {
        defaultExecutor = this

        addSyntax({ sender: CommandSender, context: CommandContext ->
            run(sender, context)
        }, chosenPalette)
    }

    private fun run(sender: CommandSender, context: CommandContext) {
        val player = sender as Player
        val sdkPlayer = SDK.players.get(player.uuid)

        val palette = context.get(chosenPalette)

        val foundPalette = PaletteType.entries.find {
            it.name.prettify() == palette
        } ?: return

        sdkPlayer.setPalette(foundPalette)

        val currentPaletteType = PaletteType.entries[sdkPlayer.getData().palette]
        val selectorIndex = player.inventory.itemStacks.indexOfFirst { item -> item.get(ItemComponent.CUSTOM_NAME) == Constants.paletteSelectorName }
        player.inventory.setItemStack(selectorIndex, player.inventory.getItemStack(selectorIndex).withMaterial(
            PaletteUtil.getIconBlock(currentPaletteType)))

        player.sendMessage(Translations.translate("core.commands.palette.message", palette))
    }

    override fun apply(sender: CommandSender, context: CommandContext) {}
}