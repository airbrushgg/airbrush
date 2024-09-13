

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

import gg.airbrush.core.commands.mainmenu.openMainMenu
import gg.airbrush.core.commands.mainmenu.openPaletteGUI
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.players.PaletteType
import gg.airbrush.server.lib.mm
import gg.airbrush.core.lib.GUIItems
import gg.airbrush.core.lib.prettify
import gg.airbrush.core.lib.toMaterial
import gg.airbrush.pocket.GUI
import gg.airbrush.sdk.NotFoundException
import gg.airbrush.sdk.lib.Translations
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minestom.server.component.DataComponentMap
import net.minestom.server.entity.Player
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.*
import net.minestom.server.item.component.EnchantmentList
import net.minestom.server.item.enchant.Enchantment

fun openBrushGUI(p: Player) {
    val sdkPlayer = SDK.players.get(p.uuid)
    val playerData = sdkPlayer.getData()

    fun generatePalette(chosenBlock: Block): List<ItemStack> {
        val paletteType = PaletteType.entries.getOrNull(playerData.palette)
            ?: throw Exception("Palette (${playerData.palette}) not found.")
        val progression = sdkPlayer.getPaletteProgression(paletteType)
            ?: throw Exception("Failed to fetch progression for ${playerData.uuid}.")

        val itemStacks = mutableListOf<ItemStack>()

        fun createItem(material: Material, name: String): ItemStack {
            val builder = ItemStack.builder(material)
                .customName(name.mm())
                .lore(Translations.translate("core.brush_gui.item_lore").mm())

            if (material.key() == chosenBlock.key())
                builder.set(ItemComponent.ENCHANTMENTS, EnchantmentList(mapOf(
                    Enchantment.EFFICIENCY to 10
                ), false))

            return builder.build()
        }

        val blocks = if(paletteType == PaletteType.CONCRETE)
            SDK.palettes.get(paletteType)
            else SDK.palettes.getBlocks(paletteType, progression.index)

        blocks.map {
            val block = it.toMaterial()
                ?: throw NotFoundException("$it is an invalid material.")
            itemStacks.add(createItem(block, block.name().prettify()))
        }

        return itemStacks
    }

    fun changeRadius(increment: Boolean, clickedSlot: Int) {
        val radiusInfo = playerData.brushRadius
        val newRadius = if(increment) radiusInfo.current + 1 else radiusInfo.current - 1

        if(newRadius < 1  || newRadius > 5) return

        if(newRadius > radiusInfo.max) {
            p.sendMessage(Translations.translate("core.brush_gui.locked_radius", newRadius).mm())
            return
        }

        val inventory = p.openInventory ?: return
        val prevDecreaseSlot = if (increment) clickedSlot - 1 else clickedSlot
        val prevIncreaseSlot = if (increment) clickedSlot else clickedSlot + 1

        inventory.replaceItemStack(prevDecreaseSlot) { stack -> stack.withAmount(newRadius) }
        inventory.replaceItemStack(prevIncreaseSlot) { stack -> stack.withAmount(newRadius) }

        try {
            sdkPlayer.setRadius(newRadius)
        } catch (ex: Exception) {
            p.sendMessage("<error>${ex.message}".mm())
        }
    }

    fun getBorderMaterial(): Material? {
        val namespace = playerData.chosenBlock
        val startIndex = namespace.indexOf(':') + 1
        var endIndex = namespace.indexOf('_')

        if (namespace.contains("light_")) {
            endIndex = namespace.indexOf('_', endIndex + 1)
        }

        val color = namespace.substring(startIndex, endIndex)
        return Material.fromNamespaceId("minecraft:${color}_stained_glass_pane")
    }

    val template = """
              xxxxxxxxx
              xyyyyyyyx
              xyyyyyyyx
              xyyyyyyyx
              xyyyyyyyx
              x<>xcbxxx
        """.trimIndent()

    val inventory = GUI(template, Translations.translate("core.brush_gui.title"), InventoryType.CHEST_6_ROW)

    val borderMaterial = getBorderMaterial() ?: Material.GLASS_PANE
    inventory.put('x', ItemStack.builder(borderMaterial)
        .customName("<green>".mm())
        .build()
    )

    inventory.put('c', GUIItems.mainMenu) {
        openMainMenu(player = p)
    }

    inventory.put('b', GUIItems.paletteSelection) {
        openPaletteGUI(p)
    }

    inventory.put('<', ItemStack.builder(Material.ENDER_PEARL)
        .customName(Translations.translate("core.brush_gui.decrease_size").mm())
        .amount(playerData.brushRadius.current)
        .build()
    ) {
        changeRadius(increment = false, it.slot)
        p.playSound(
            Sound.sound(Key.key("block.tripwire.attach"), Sound.Source.MASTER, 1.0f, 0.9f),
            Sound.Emitter.self()
        )
    }

    inventory.put('>', ItemStack.builder(Material.ENDER_EYE)
        .customName(Translations.translate("core.brush_gui.increase_size").mm())
        .amount(playerData.brushRadius.current)
        .build()
    ) {
        changeRadius(increment = true, it.slot)
        p.playSound(
            Sound.sound(Key.key("block.tripwire.attach"), Sound.Source.MASTER, 1.0f, 1.1f),
            Sound.Emitter.self()
        )
    }

    val chosenBlock = Block.values().find {
        it.key().toString() == playerData.chosenBlock
    } ?: return


    // TODO: Create a method for this later on down the line.

    val palette = generatePalette(chosenBlock).toMutableList()

    var paletteIndex = 0
    var index = 0

    for (row in template.lines()) {
        for (char in row) {
            if (char == 'y' && paletteIndex < palette.size) {
                val item = palette[paletteIndex]
                inventory.put(index, item) {
                    val clickedKey = item.material().key().toString()
                    sdkPlayer.setChosenBlock(clickedKey)
                    p.sendActionBar(Translations.translate("core.brush_gui.set_block", clickedKey.prettify()).mm())
                    p.closeInventory()

                    p.playSound(
                        Sound.sound(Key.key("item.bucket.fill"), Sound.Source.MASTER, 1.0f, 1.0f),
                        Sound.Emitter.self()
                    )
                }
                paletteIndex++
            }
            index++
        }
    }

    inventory.open(p)
}