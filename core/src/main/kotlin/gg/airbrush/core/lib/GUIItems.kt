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