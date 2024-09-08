package gg.airbrush.core.commands.mainmenu

import gg.airbrush.core.commands.openBrushGUI
import gg.airbrush.core.lib.Constants
import gg.airbrush.core.lib.GUIItems
import gg.airbrush.core.lib.PaletteUtil
import gg.airbrush.pocket.GUI
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.palettes.Palettes
import gg.airbrush.sdk.classes.players.PaletteType
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import kotlin.math.roundToInt

fun openPaletteGUI(p: Player) {
    val sdkPlayer = SDK.players.get(p.uuid)

    val template = """
              xxxxxxxxx
              xaaaaaaax
              xa12345ax
              xaaaaaaax
              xaaaaaaax
              xxxxcbxxx
    """.trimIndent()

    val inventory = GUI(template, Translations.translate("core.select_palette.title"), InventoryType.CHEST_6_ROW)

    inventory.put('x', GUIItems.glass)

    inventory.put('b', GUIItems.brushes) {
        openBrushGUI(p)
    }

    inventory.put('c', GUIItems.mainMenu) {
        openMainMenu(p)
    }

    fun generateProgressBar(maxChars: Int, value: Int, total: Int): String {
        val percent = value.toDouble() * 100.0 / total
        val filled = value * maxChars / total
        return "<p>[<#c24cff>${"|".repeat(filled)}<s>${"|".repeat(maxChars - filled)}<p>] ${percent.roundToInt()}%"
    }

    fun createPaletteItem(type: PaletteType, amountProgressed: Int): ItemStack {
        val material = PaletteUtil.getIconBlock(type)
        val paletteName = PaletteUtil.getName(type)

        val paletteSize = Palettes().get(type).size
	    val itemLore = mutableListOf(
		    "<s>Progress: ${generateProgressBar(24, amountProgressed, paletteSize)}".mm()
		)

	    if(amountProgressed < paletteSize) {
			itemLore.add("<p>➜ <em>Right-click to progress!".mm())
		}

        return ItemStack.builder(material)
            .customName(Translations.translate("core.select_palette.item", paletteName).mm())
            .lore(itemLore)
            .build()
    }

    val progression = sdkPlayer.getData().paletteProgression
    PaletteType.entries.forEachIndexed { index, paletteType ->
        val i = index + 1

        val char = i.digitToChar()
        val paletteName = PaletteUtil.getName(paletteType)

        val progressionInfo = progression.find { it.paletteType == paletteType.ordinal }
        val amountProgressed = progressionInfo?.index ?: 0
	    val paletteSize = Palettes().get(paletteType).size

        inventory.put(char, createPaletteItem(paletteType, amountProgressed)) {
			if(it.clickType == ClickType.RIGHT_CLICK && amountProgressed < paletteSize) {
				sdkPlayer.setProgressionPalette(paletteType)
				p.sendActionBar(Translations.translate("core.select_palette.progression_success", paletteName).mm())
			} else {
				sdkPlayer.setPalette(paletteType)

				val currentPaletteType = PaletteType.entries[sdkPlayer.getData().palette]
				val selectorIndex = p.inventory.itemStacks.indexOfFirst { item -> item.get(ItemComponent.CUSTOM_NAME) == Constants.paletteSelectorName }
				p.inventory.setItemStack(selectorIndex, p.inventory.getItemStack(selectorIndex).withMaterial(
					PaletteUtil.getIconBlock(currentPaletteType)))

				p.sendActionBar(Translations.translate("core.select_palette.set_palette", paletteName).mm())
			}
        }
    }

    inventory.open(p)
}