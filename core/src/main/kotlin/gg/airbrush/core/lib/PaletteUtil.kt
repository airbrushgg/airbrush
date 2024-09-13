

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

import gg.airbrush.sdk.classes.players.PaletteType
import net.minestom.server.item.Material

object PaletteUtil {
    fun getIconBlock(type: PaletteType): Material {
        val material = when(type) {
            PaletteType.CONCRETE -> Material.RED_CONCRETE
            PaletteType.WOOL -> Material.ORANGE_WOOL
            PaletteType.CONCRETE_POWDER -> Material.YELLOW_CONCRETE_POWDER
            PaletteType.TERRACOTTA -> Material.GREEN_TERRACOTTA
            PaletteType.GLAZED_TERRACOTTA -> Material.BLUE_GLAZED_TERRACOTTA
        }
        return material
    }

    fun getName(type: PaletteType): String {
        return type.name.prettify()
    }
}