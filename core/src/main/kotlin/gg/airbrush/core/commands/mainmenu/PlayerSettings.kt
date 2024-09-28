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

import gg.airbrush.core.lib.GUIItems
import gg.airbrush.pocket.GUI
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.executeCommand
import gg.airbrush.server.lib.mm
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

fun openPlayerSettings(player: Player) {
    val template = """
        xxxxxxxxx
        xaaawaaax
        xxxxcxxxx
    """.trimIndent()
    val gui = GUI(template, "Your Settings", InventoryType.CHEST_3_ROW)

    gui.put('x', GUIItems.glass)

    gui.put('c', GUIItems.mainMenu) {
        openMainMenu(player)
    }

    val playerWorld = SDK.worlds.getByOwner(player.uuid.toString())

    val worldSettings = ItemStack.builder(Material.ENDER_EYE)
        .customName("<p>World Settings".mm())
        .lore(when {
            playerWorld != null -> listOf("<s>Manage your custom world settings".mm())
            player.hasPermission("core.createworld") -> listOf("<s>You don't have a player world! Create one with <p>/create<s>.".mm())
            else -> listOf(
                "<s>This feature is only available to <donator>Superstars</donator>!".mm(),
                "<s>Buy a <donator>[⭐⭐]</donator> rank in /buy for access.".mm()
            )
        })
        .build()
    gui.put('w', worldSettings) { event ->
        event.player.executeCommand("world")
    }

    gui.open(player)
}