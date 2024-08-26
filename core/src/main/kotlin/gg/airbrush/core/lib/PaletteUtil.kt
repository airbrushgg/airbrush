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