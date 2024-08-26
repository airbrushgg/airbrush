package gg.airbrush.pocket

import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.event.inventory.InventoryCloseEvent

typealias ClickHandler = (InventoryClickEvent) -> Unit
typealias CloseHandler = (InventoryCloseEvent) -> Unit