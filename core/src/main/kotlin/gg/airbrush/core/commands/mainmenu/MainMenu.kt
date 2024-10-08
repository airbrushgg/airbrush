

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

package gg.airbrush.core.commands.mainmenu

import gg.airbrush.core.lib.ColorUtil
import gg.airbrush.core.lib.GUIItems
import gg.airbrush.pocket.GUI
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.executeCommand
import gg.airbrush.server.lib.mm
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.color.Color
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.HeadProfile

fun openMainMenu(player: Player) {
    val sdkPlayer = SDK.players.get(player.uuid)

    val template = """
              xxxxxxxxx
              xaaapaaax
              xaaaaaaax
              xabaeawax
              xaaaaaaax
              xxxxxxxxx
        """.trimIndent()

    val inventory = GUI(template, "Main Menu", InventoryType.CHEST_6_ROW)
    inventory.put('x', GUIItems.glass)

    val level = sdkPlayer.getLevel()
    val levelColor = ColorUtil.oscillateHSV(Color(0xff0000), Color(0xff00ff), level)
    val rankData = sdkPlayer.getRank().getData()
    val prefix = rankData.prefix

    val builder = ItemStack.builder(Material.PLAYER_HEAD)
        .customName("<p>Player Settings".mm())
        .lore(
            "<s>View or change player information".mm(),
            "<s>and world settings".mm(),
            Component.empty(),
            "<s>Rank: ${if (prefix.isNotEmpty()) prefix.uppercase() else "Default"}".mm(),
            "<s>Level: <${TextColor.color(levelColor).asHexString()}>[$level]".mm()
        )

    player.skin?.let { builder.set(ItemComponent.PROFILE, HeadProfile(it)) }
    val settings = builder.build()

    inventory.put('p', settings) {
        openPlayerSettings(player)
    }

    val boosters = ItemStack.builder(Material.FIRE_CHARGE)
        .customName("<p>Boosters".mm())
        .lore("<s>View or activate your XP boosters".mm())
        .build()
    inventory.put('b', boosters) {
        Boost.openBoostersGui(player)
    }

    inventory.put('w', GUIItems.worldSelection) {
        openWorldGUI(player)
    }

    val shop = ItemStack.builder(Material.NETHER_STAR)
        .customName("<p>Shop".mm())
        .lore("<s>Buy XP boosters or ranks for more perks".mm())
        .build()
    inventory.put('e', shop) {
	    player.closeInventory()
        player.executeCommand("shop")
    }

    inventory.open(player)
}

class MainMenu : Command("mainmenu", "mm"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val player = sender as Player
        openMainMenu(player)
    }
}