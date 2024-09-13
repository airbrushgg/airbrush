

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

package gg.airbrush.core.lib

import gg.airbrush.server.lib.mm
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

object GUIItems {
    val glass = ItemStack.builder(Material.MAGENTA_STAINED_GLASS_PANE)
        .customName("<green>".mm())
        .build()

    val brushes = ItemStack.builder(Material.FEATHER)
        .customName("<p>View brush settings".mm())
        .build()

    val mainMenu = ItemStack.builder(Material.COMPASS)
        .customName("<p>Go to main menu".mm())
        .build()

    val worldSelection = ItemStack.builder(Material.FILLED_MAP)
        .customName("<p>Worlds".mm())
        .lore("<s>View personal worlds made by other players".mm())
        .build()

    val paletteSelection = ItemStack.builder(Material.BUCKET)
        .customName("<p>View available palettes".mm())
        .build()
}