/*
 * This file is part of Airbrush
 *
 * Copyright (c) Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.core.commands.worlds

import gg.airbrush.core.commands.mainmenu.openMainMenu
import gg.airbrush.core.events.sidebars
import gg.airbrush.core.lib.CanvasManager
import gg.airbrush.core.lib.GUIItems
import gg.airbrush.core.lib.getWorldLine
import gg.airbrush.core.lib.teleportToCanvas
import gg.airbrush.pocket.GUI
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.worlds.WorldVisibility
import gg.airbrush.sdk.lib.*
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentEnum
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class WorldSettings : Command("worldsettings", "world"), CommandExecutor {
    init {
        defaultExecutor = this
        setCondition { sender, _ ->
            sender.hasPermission("core.createworld")
        }
        addSubcommand(PrivacySubcommand())
        addSubcommand(WorldNameSubcommand())
        addSubcommand(WorldDeleteSubcommand())
    }

    override fun apply(player: CommandSender, context: CommandContext) {
        if(player !is Player) return

        val playerWorld = SDK.worlds.getByOwner(player.uuid.toString())

        if(playerWorld == null) {
            player.sendMessage("<error>You do not have a player world! Create one with <p>/create</p>.".mm())
            return
        }

        val template = """
            xxxxxxxxx
            xaxbxcxdx
            xxxxyxxxx
        """.trimIndent()
        val gui = GUI(template, "World Settings", InventoryType.CHEST_3_ROW)

        gui.put('x', GUIItems.glass)

        gui.put('y', GUIItems.mainMenu) {
            openMainMenu(player)
        }

        val changeName = ItemStack.builder(Material.NAME_TAG)
            .customName("<p>World Name".mm())
            .lore(
                "<s>Change your world name!".mm(),
                "<s>Current value: <p>${playerWorld.data.name}".mm(),
                Component.empty(),
                "<#ffb5cf> ➜ <#ffd4e3>Click to change!".mm()
            )
            .build()

        gui.put('a', changeName) {
            player.closeInventory()
            player.sendMessage("<s>Enter your new world name below:".mm())
            fetchInput(player) {
                player.executeCommand("world rename $it")
            }.prompt()
        }

        var visibility = playerWorld.data.visibility
        fun generatePrivacyLore() = listOf(
            "<s>Choose whether your world is public or private!".mm(),
            "<s>Current value: <p>${visibility.name.lowercase()}".mm(),
            Component.empty(),
            "<#ffb5cf> ➜ <#ffd4e3>Click to change!".mm()
        )
        val changePrivacy = ItemStack.builder(Material.ENDER_EYE)
            .customName("<p>World Privacy".mm())
            .lore(generatePrivacyLore())

        gui.put('b', changePrivacy.build()) {
            visibility = when (visibility) {
                WorldVisibility.PUBLIC -> WorldVisibility.PRIVATE
                WorldVisibility.PRIVATE -> WorldVisibility.PUBLIC
            }
            player.executeCommand("world privacy ${visibility.name.lowercase()}")
            val inv = it.inventory ?: return@put
            inv.setItemStack(it.slot, changePrivacy.lore(generatePrivacyLore()).build())
        }

        var gamemode = player.gameMode
        fun generateGamemodeLore() = listOf(
            "<s>${if (gamemode == GameMode.CREATIVE) "➜" else ""} Creative".mm(),
            "<s>${if (gamemode == GameMode.SURVIVAL) "➜" else ""} Survival".mm(),
            "<s>${if (gamemode == GameMode.SPECTATOR) "➜" else ""} Spectator".mm(),
            Component.empty(),
            "<#ffb5cf> ➜ <#ffd4e3>Click to change!".mm()
        )
        val changeGamemodeItem = ItemStack.builder(Material.COMMAND_BLOCK_MINECART)
            .customName("<p>Change Your Gamemode".mm())
            .lore(generateGamemodeLore())

        gui.put('c', changeGamemodeItem.build()) {
            val canvasInstance = CanvasManager.get(playerWorld.data.id)

            if(canvasInstance == null) {
                player.sendMessage("<error>You do not have a player world! Create one with <p>/create</p>.".mm())
                return@put
            }

            if(player.instance.uniqueId !== canvasInstance.uniqueId) {
                player.sendMessage("<error>You must be in your world to change gamemode!".mm())
                return@put
            }

            gamemode = when (gamemode) {
                GameMode.CREATIVE -> GameMode.SURVIVAL
                GameMode.SURVIVAL -> GameMode.SPECTATOR
                GameMode.SPECTATOR -> GameMode.CREATIVE
                else -> GameMode.CREATIVE
            }
            player.gameMode = gamemode
            val inv = it.inventory ?: return@put
            inv.setItemStack(it.slot, changeGamemodeItem.lore(generateGamemodeLore()).build())
        }

        val deleteWorld = ItemStack.builder(Material.BARRIER)
            .customName("<p>Delete World".mm())
            .lore("<s>Click below to delete your world.".mm())
            .build()
        gui.put('d', deleteWorld) {
            player.closeInventory()
            player.executeCommand("world delete")
        }

        gui.open(player)
    }

    private class PrivacySubcommand : Command("privacy"), CommandExecutor {
        private val typeArgument = ArgumentType.Enum("type", WorldVisibility::class.java)
            .setFormat(ArgumentEnum.Format.LOWER_CASED)

        init {
            defaultExecutor = this
            addSyntax(this, typeArgument)
        }

        override fun apply(player: CommandSender, context: CommandContext) {
            if(player !is Player) return

            val privacyType = context.get(typeArgument)
            val playerWorld = SDK.worlds.getByOwner(player.uuid.toString())

            if(playerWorld === null) {
                player.sendMessage("<error>You do not have a player world! Create one with <p>/create</p>.".mm())
                return
            }

            playerWorld.changeVisibility(privacyType)

            player.sendMessage("<success>Changed your world privacy to ${privacyType.name.lowercase()}".mm())
        }
    }

    private class WorldNameSubcommand : Command("rename"), CommandExecutor {
        private val nameArgument = ArgumentType.StringArray("name")
        init {
            defaultExecutor = this
            addSyntax(this, nameArgument)
        }

        override fun apply(player: CommandSender, context: CommandContext) {
            if(player !is Player) return

            val playerWorld = SDK.worlds.getByOwner(player.uuid.toString())

            if(playerWorld == null) {
                player.sendMessage("<error>You do not have a player world! Create one with <p>/create</p>.".mm())
                return
            }

            val newName = context.get(nameArgument).joinToString(" ")

            if(newName.length > 16) {
                player.sendMessage("<error>Your world name cannot be longer than 16 characters!".mm())
                return
            }

            playerWorld.rename(newName)

            player.sendMessage("<success>Changed your world name to $newName".mm())
            sidebars[player.uuid]?.updateLineContent("world", getWorldLine(player))

            if(player.openInventory != null) player.closeInventory()
        }
    }

    private class WorldDeleteSubcommand : Command("delete"), CommandExecutor {
        private val confirmArgument = ArgumentType.String("confirm")
        init {
            defaultExecutor = this
            addSyntax(this)
            addSyntax(this, confirmArgument)
        }

        override fun apply(player: CommandSender, context: CommandContext) {
            if(player !is Player) return
            val confirm = context.get(confirmArgument)

           if(confirm.isNullOrEmpty()) {
                player.sendMessage("<s>Please enter <g>yes</g> to confirm:".mm())
                fetchInput(player) {
                    if(!isConfirmed(it)) {
                        player.sendMessage("<error>Cancelled deleting world.".mm())
                        return@fetchInput
                    }

                    player.executeCommand("world delete confirm")
                }.prompt()
                return
            } else if(!isConfirmed(confirm)) {
               player.sendMessage("<error>Cancelled deleting world.".mm())
               return
           }

            val playerWorld = SDK.worlds.getByOwner(player.uuid.toString())

            if(playerWorld == null) {
                player.sendMessage("<error>You do not have a player world! Create one with <p>/create</p>.".mm())
                return
            }

            val canvasInstance = CanvasManager.get(playerWorld.data.id)!!

            if(player.instance.uniqueId == canvasInstance.uniqueId) {
                player.setInstance(WorldManager.defaultInstance)
            }

            delay(500) {
                playerWorld.delete()
                WorldManager.deleteCanvas(playerWorld.data.id)
                player.sendMessage("<success>Deleted your world!".mm())
            }
        }
    }
}