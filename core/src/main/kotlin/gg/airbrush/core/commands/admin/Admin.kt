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

package gg.airbrush.core.commands.admin

import gg.airbrush.core.lib.CanvasManager
import gg.airbrush.core.lib.teleportToSpawn
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.PlayerUtils
import gg.airbrush.sdk.lib.delay
import gg.airbrush.sdk.lib.fetchInput
import gg.airbrush.sdk.lib.isConfirmed
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.tag.Tag
import java.util.UUID

class Admin : Command("admin", "a"), CommandExecutor {
    init {
        defaultExecutor = this
        setCondition { sender, _ -> sender.hasPermission("core.admin") }
        addSubcommand(WorldInfo())
        addSubcommand(DeleteWorld())
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if(sender !is Player) return

        sender.sendMessage("<s>Admin commands:".mm())
        sender.sendMessage("<s>➜ <p>/addbooster <s>Adds a booster to a player.".mm())
    }

    private class WorldInfo: Command("worldinfo", "wi"), CommandExecutor {
        init {
            defaultExecutor = this
            setCondition { sender, _ -> sender.hasPermission("core.admin") }
        }

        override fun apply(player: CommandSender, context: CommandContext) {
            if(player !is Player) return
            val canvasId = player.instance.getTag(Tag.String("CanvasUUID")) ?: null

            if(canvasId == null) {
                player.sendMessage("<error>You are not in a player world!".mm())
                return
            }

            val world = SDK.worlds.getByUUID(canvasId) ?: return
            val worldCreator = PlayerUtils.getName(UUID.fromString(world.data.ownedBy))

            player.sendMessage("""
                
                <s>World Info:
                
                World ID: <p><click:copy_to_clipboard:${world.data.id}>${world.data.id}</click></p>
                World Name: <p>${world.data.name}</p>
                World Owner: <p><click:copy_to_clipboard:$worldCreator>$worldCreator</click></p>
                World Visibility: <p>${world.data.visibility.name.lowercase()}</p>
                
                Click <b><click:run_command:/admin deleteworld ${world.data.id}>here</click></b> to delete this world.
                
            """.trimIndent().mm())
        }
    }

    private class DeleteWorld: Command("deleteworld", "dw"), CommandExecutor {
        private val idArgument = ArgumentType.UUID("id")
        init {
            defaultExecutor = this
            setCondition { sender, _ -> sender.hasPermission("core.admin") }
            addSyntax(this, idArgument)
        }

        override fun apply(sender: CommandSender, context: CommandContext) {
            if(sender !is Player) return

            val id = context.get(idArgument)
            val world = SDK.worlds.getByUUID(id) ?: return

            sender.sendMessage("""
                <s>Are you sure you want to delete the ${world.data.name} world? This action cannot be undone!
               
                <error>Please type <b>yes</b> to confirm:
            """.trimIndent().mm())

            fetchInput(sender) {
                if(!isConfirmed(it)) {
                    sender.sendMessage("<error>Cancelled deleting world.".mm())
                    return@fetchInput
                }

                val canvasInstance = CanvasManager.get(id.toString())!!

                canvasInstance.players.forEach { p ->
                    p.teleportToSpawn()
                }

                delay(500) {
                   world.delete()
                   WorldManager.deleteCanvas(id.toString())
                }

                sender.sendMessage("<success>Deleted world!".mm())
            }.prompt()
        }
    }
}