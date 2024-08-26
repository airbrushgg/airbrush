package gg.airbrush.core.commands.mainmenu

import gg.airbrush.core.events.sidebars
import gg.airbrush.core.lib.GUIItems
import gg.airbrush.core.lib.getWorldLine
import gg.airbrush.pocket.GUI
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.worlds.WorldVisibility
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

    val worldPrivacy = ItemStack.builder(Material.ENDER_EYE)
        .customName("<p>World Privacy".mm())
        .lore(
            "<s>Choose whether your world is public or private!".mm(),
            if (playerWorld != null) {
                "<s>Current value: <p>${playerWorld.data.visibility.name.lowercase()}".mm()
            } else {
                "<s>You don't have a player world! Create one with <p>/create<s>.".mm()
            },
            "".mm(),
            "<#ffb5cf> • <#ffd4e3>Click to change!".mm()
        )
        .build()
    gui.put('w', worldPrivacy) { event ->
        if (playerWorld == null) {
            player.sendMessage("<s>You do not have a player world! If you are a donator, you can create one with <p>/create<s>.".mm())
            player.closeInventory()
            return@put Unit
        }

        playerWorld.changeVisibility(when (playerWorld.data.visibility) {
            WorldVisibility.PUBLIC -> WorldVisibility.PRIVATE
            WorldVisibility.PRIVATE -> WorldVisibility.PUBLIC
        })

	    val sidebar = sidebars[player.uuid]
	    if(sidebar !== null) {
		    sidebar.updateLineContent("world", getWorldLine(player))
	    }

        event.inventory?.setItemStack(event.slot, event.clickedItem.withLore(
            listOf(
                "<s>Choose whether your world is public or private!".mm(),
                "<s>Current value: <p>${playerWorld.data.visibility.name.lowercase()}".mm(),
                "".mm(),
                "<#ffb5cf> • <#ffd4e3>Click to change!".mm()
            )
        ))
    }

    gui.open(player)
}