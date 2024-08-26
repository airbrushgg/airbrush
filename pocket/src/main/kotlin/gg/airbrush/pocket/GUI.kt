package gg.airbrush.pocket

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.EventListener
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.event.inventory.InventoryCloseEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import kotlin.math.max
import kotlin.math.min

open class GUI(
    private val template: String,
    title: String,
    type: InventoryType = InventoryType
        .valueOf("CHEST_${min(6, max(template.trimIndent().split("\n").size, 1))}_ROW")
) {
    private val lines = template.trimIndent().split("\n")
    private val clickHandlers = mutableMapOf<Int, ClickHandler>()
    val inventory = Inventory(type, title)

    init {
        if (lines.isEmpty())
            throw IllegalArgumentException("Received empty template")

        if (lines.find { it.length != lines[0].length } != null)
            throw IllegalArgumentException("Received template with inconsistent row length")

        val rows = lines.size
        val columns = lines[0].length

        if (type.name.contains("CHEST")) {
            if (rows > 6)
                throw IllegalArgumentException("Received chest template with invalid amount of rows ($rows)")

            if (columns != 9)
                throw IllegalArgumentException("Received chest template with invalid amount of columns ($columns)")
        } else if (lines.joinToString("").length != type.size)
            throw IllegalArgumentException("Received template with invalid size")

        inventory.addInventoryCondition { player, slot, clickType, result ->
            if (slot !in clickHandlers)
                return@addInventoryCondition

            result.isCancel = true
            clickHandlers[slot]!!.invoke(InventoryClickEvent(
                inventory,
                player,
                slot,
                clickType,
                result.clickedItem,
                result.cursorItem))
        }
    }

    fun put(char: Char, item: ItemStack, handler: ClickHandler = {}): GUI {
        val template = template.replace(Regex("\\s+"), "")

        for ((index, templateChar) in template.withIndex()) {
            if (templateChar != char)
                continue

            inventory.setItemStack(index, item)
            clickHandlers[index] = handler
        }

        return this
    }

    fun put(index: Int, item: ItemStack, handler: ClickHandler = {}): GUI {
        inventory.setItemStack(index, item)
        clickHandlers[index] = handler
        return this
    }

    fun open(player: Player, handler: CloseHandler = {}): GUI {
        player.openInventory(inventory)

        val manager = MinecraftServer.getGlobalEventHandler()

        val clickHandler = EventListener.of(InventoryClickEvent::class.java) { event ->
            val inventory = event.inventory
            val eventPlayer = event.player
            val slot = event.slot

            if (eventPlayer != player || inventory != this.inventory)
                return@of

            clickHandlers[slot]?.invoke(event)
        }

        lateinit var closeHandler: EventListener<InventoryCloseEvent>
        closeHandler = EventListener.of(InventoryCloseEvent::class.java) { event ->
            val inventory = event.inventory
            val eventPlayer = event.player

            if (eventPlayer != player || inventory != this.inventory)
                return@of

            manager.removeListener(clickHandler)
            manager.removeListener(closeHandler)
            handler(event)
        }

        manager.addListener(clickHandler)
        manager.addListener(closeHandler)

        return this
    }
}