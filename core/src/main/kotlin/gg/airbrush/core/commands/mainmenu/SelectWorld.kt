

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
import gg.airbrush.core.lib.teleportToCanvas
import gg.airbrush.core.lib.teleportToSpawn
import gg.airbrush.pocket.GUI
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.worlds.WorldVisibility
import gg.airbrush.sdk.lib.PlayerUtils
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

fun openWorldGUI(p: Player) {
    // s = spawn
    // d = donator
    // y = player world

    // v = custom world
    // c = compass

    val template = """
              xxxxxxxxx
              xsdyyyyyx
              xyyyyyyyx
              xyyyyyyyx
              xxxxxxxxx
              xxxvxcxxx
    """.trimIndent()

    val inventory = GUI(template, "Select a World", InventoryType.CHEST_6_ROW)

    inventory.put('x', GUIItems.glass)

    inventory.put('c', GUIItems.mainMenu) {
        openMainMenu(p)
    }

    val spawnWorld = ItemStack.builder(Material.CHERRY_SIGN)
        .customName("<p>Spawn World".mm())
        .lore(
            "<s>This is the public canvas world for all players!".mm(),
            "".mm(),
            "<#ffb5cf> • <#ffd4e3>Click to teleport!".mm()
        )
        .build()

    inventory.put('s', spawnWorld) {
        p.sendMessage("<s>Teleporting you to the spawn world!".mm())
        p.teleportToSpawn()
    }

    val starWorld = ItemStack.builder(Material.WARPED_SIGN)
        .customName("<p>Star World".mm())
        .lore(
            "<s>This is an <donator>exclusive</donator> canvas only for donators!".mm(),
            "<s>Buy a <donator>[⭐]</donator> or <donator>[⭐⭐]</donator> rank in the shop for access.".mm(),
            "".mm(),
            "<#ffb5cf> • <#ffd4e3>Click to teleport!".mm()
        )
        .build()
    inventory.put('d', starWorld) {
		if(!p.hasPermission("core.donor")) {
			p.sendMessage("<error>You don't have Star access, so you can't access this world!".mm())
			return@put
		}

        val starWorldInstance = WorldManager.getPersistentWorld("star_world")
        if (starWorldInstance == null) {
            MinecraftServer.LOGGER.error("Failed to fetch Star world!")
            p.sendMessage("<error>A problem occurred teleporting to this world!".mm())
            return@put
        }
        p.sendMessage("<s>Teleporting you to the Star world!".mm())
        p.setInstance(starWorldInstance)
    }

    val yourWorld = ItemStack.builder(Material.CHERRY_HANGING_SIGN)
        .customName("<p>Your World".mm())
        .build()
    inventory.put('v', yourWorld) {
        val playerWorld = SDK.worlds.getByOwner(p.uuid.toString())
        if (playerWorld == null) {
            p.sendMessage("<s>You do not have a player world! If you are a donator, you can create one with <p>/create<s>.".mm())
            return@put
        }

        p.sendMessage("<s>Teleporting you to your world!".mm())
        p.teleportToCanvas(playerWorld.data.id)
    }

    val playerWorlds = SDK.worlds.getAll().filter {
        it.data.visibility == WorldVisibility.PUBLIC
    }

    // TODO: Create a method for this later on down the line.

    var worldIndex = 0
    var index = 0

    for (row in template.lines()) {
        for (char in row) {
            if (char == 'y' && worldIndex < playerWorlds.size) {
                val world = playerWorlds[worldIndex]
                val signs = Material.values().toList().filter {
                    it.key().toString().contains("hanging_sign")
                }

                val item = ItemStack.builder(signs.random())
                    .customName(world.data.name.mm())
                    .build()

                inventory.put(index, item) {
                    val owner = PlayerUtils.getName(UUID.fromString(world.data.ownedBy))
                    p.sendMessage("<s>Teleporting you to <p>${owner}<s>'s world!".mm())
                    p.playSound(Sound.sound(Key.key("block.portal.travel"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self())
                    p.teleportToCanvas(world.data.id)
                }
                worldIndex++
            }
            index++
        }
    }

    inventory.open(p)
}

class SelectWorld : Command("worlds"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        val player = sender as Player
        openWorldGUI(player)
    }
}