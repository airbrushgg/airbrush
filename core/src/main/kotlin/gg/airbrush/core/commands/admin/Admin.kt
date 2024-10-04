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
import gg.airbrush.core.lib.getCurrentWorldID
import gg.airbrush.core.lib.getCurrentWorldName
import gg.airbrush.core.lib.teleportToSpawn
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.PlayerUtils
import gg.airbrush.sdk.lib.delay
import gg.airbrush.sdk.lib.fetchInput
import gg.airbrush.sdk.lib.isConfirmed
import gg.airbrush.server.arguments.OfflinePlayer
import gg.airbrush.server.arguments.OfflinePlayerArgument
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import kotlinx.coroutines.*
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.instance.batch.AbsoluteBlockBatch
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import java.time.Instant
import java.util.*
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class Admin : Command("admin", "a"), CommandExecutor {
    init {
        defaultExecutor = this
        setCondition { sender, _ -> sender.hasPermission("core.admin") }
        addSubcommand(WorldInfo())
        addSubcommand(DeleteWorld())
        addSubcommand(AddBooster())
        addSubcommand(ReplayHistory())
        addSubcommand(SaveWorld())
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if(sender !is Player) return

        sender.sendMessage("<s>Admin commands:".mm())
        sender.sendMessage("<s>➜ <p>/admin addbooster <s>Adds a booster to a player.".mm())
        sender.sendMessage("<s>➜ <p>/admin worldinfo <s>View world info.".mm())
        sender.sendMessage("<s>➜ <p>/admin deleteworld <s>Deletes a world.".mm())
        sender.sendMessage("<s>➜ <p>/admin replayhistory <s>Replays history to undo crash rollbacks.".mm())
        sender.sendMessage("<s>➜ <p>/admin saveworld <s>Saves the world you are in.".mm())
    }

    private class ReplayHistory: Command("replayhistory", "rh"), CommandExecutor {
        private val timeArgument = ArgumentType.StringArray("time")

        init {
            defaultExecutor = CommandExecutor { sender, _ ->
                sender.sendMessage("<error>Usage: /admin replayhistory <time>".mm())
            }

            setCondition { sender, _ -> sender.hasPermission("core.admin") }
            addSyntax(this, timeArgument)
        }

        override fun apply(sender: CommandSender, context: CommandContext) {
            if (sender !is Player)
                return sender.sendMessage("<error>You aren't a player.".mm())

            val timeString = context.get(timeArgument).joinToString(" ")
            val duration = Duration.parseOrNull(timeString) ?: run {
                sender.sendMessage("<error>Invalid time format. (Hint: Make sure to split units by spaces!)".mm())
                return
            }

            val timestamp = Instant.now().minus(duration.toJavaDuration())
            sender.sendMessage("<s>Replaying history...".mm())

            CoroutineScope(Dispatchers.IO).launch {
                val batch = AbsoluteBlockBatch()

                SDK.pixels.getHistoryByTime(timestamp, sender.getCurrentWorldID()).collect {
                    batch.setBlock(Pos(
                        it.x.toDouble(),
                        it.y.toDouble(),
                        it.z.toDouble()
                    ), Material.fromId(it.material)!!.block())
                }

                batch.apply(sender.instance) {
                    sender.sendMessage("<success>Finished replaying history.".mm())
                    runBlocking {
                        MinecraftServer.LOGGER.info("[Core] Saving chunks for default world...")
                        WorldManager.defaultInstance.saveChunksToStorage().join()
                    }
                }
            }
        }
    }

    private class SaveWorld: Command("saveworld", "sw"), CommandExecutor {
        init {
            setCondition { sender, _ -> sender.hasPermission("core.admin") }
            addSyntax(this)
        }

        override fun apply(sender: CommandSender, context: CommandContext) {
            if(sender !is Player) return

            val worldName = sender.getCurrentWorldName()
            sender.instance.saveChunksToStorage().join()

            sender.sendMessage("<success>Saved the $worldName<reset><g> world!".mm())

            val worldId = sender.getCurrentWorldID()
            MinecraftServer.LOGGER.info("[Core] Forcefully saved the $worldId world.")
        }
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

    private class AddBooster : Command("addbooster", "ab"), CommandExecutor {
        private val registeredBoosters = SDK.boosters.getAvailableBoosters()
        private val idArgument = ArgumentType.String("id")
        private val amountArgument = ArgumentType.Integer("amount")
        private val playerArgument = OfflinePlayerArgument("offline-player")

        init {
            defaultExecutor = CommandExecutor { sender, _ ->
                sender.sendMessage("<error>/admin addbooster <player> <id> [amount]".mm())
            }

            addSyntax(this::apply, playerArgument, idArgument, amountArgument)
            addSyntax(this::apply, playerArgument, idArgument)
        }

        override fun apply(sender: CommandSender, context: CommandContext) = runBlocking {
            val offlinePlayer = context.get<Deferred<OfflinePlayer>>("offline-player").await()
            val id = context.get<String>("id")
            val amount = context.get<Int>("amount") ?: 1

            val boosterInfo = registeredBoosters.find { b -> b.id == id }

            if(boosterInfo == null) {
                sender.sendMessage("<error>No such booster exists with that id!".mm())
                return@runBlocking
            }

            val playerExists = SDK.players.exists(offlinePlayer.uniqueId)
            if(!playerExists) {
                sender.sendMessage("<error>That player has never played Airbrush!".mm())
                return@runBlocking
            }

            val sdkPlayer = SDK.players.get(offlinePlayer.uniqueId)
            for (i in 0 until amount) {
                sdkPlayer.addBooster(boosterInfo)
            }

            sender.sendMessage("<success>They were given ${boosterInfo.name}!".mm())
        }
    }
}