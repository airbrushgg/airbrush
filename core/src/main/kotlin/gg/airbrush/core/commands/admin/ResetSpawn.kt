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

package gg.airbrush.core.commands.admin

import dev.flavored.bamboo.SchematicReader
import gg.airbrush.core.lib.Constants
import gg.airbrush.core.lib.format
import gg.airbrush.core.lib.getCurrentWorldID
import gg.airbrush.sdk.SDK
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import kotlinx.coroutines.*
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import kotlin.system.measureTimeMillis

class ResetSpawn : Command("resetspawn"), CommandExecutor {
    private val schematicReader = SchematicReader()

    init {
        defaultExecutor = this
        setCondition { sender, _ -> sender.hasPermission("core.admin") }

        val confirmFlag = ArgumentLiteral("confirm")
        addSyntax(this::executeConfirm, confirmFlag)
    }

    private fun executeConfirm(sender: CommandSender, context: CommandContext) {
        if (sender !is Player)
            return

        val schematicFile = Constants.schematicFolder.resolve("spawn.schem")
        if (!schematicFile.exists()) {
            sender.sendMessage("<error>Could not resolve 'spawn.schem' file.".mm())
            return
        }

        val instance = sender.instance
        if (sender.instance != WorldManager.defaultInstance) {
            sender.sendMessage("<error>You must be in the spawn world to reset it.".mm())
            return
        }

        sender.sendMessage("<p>Resetting the spawn world...".mm())

        CoroutineScope(Dispatchers.IO).launch {
            val timeTaken = measureTimeMillis {
                SDK.pixels.wipeHistoryForWorld(sender.getCurrentWorldID())

                val center = Pos(0.0, 4.0, 0.0)
                val schematic = schematicReader.fromPath(schematicFile.toPath())
                schematic.paste(instance, center, true)

                runBlocking {
                    instance.saveChunksToStorage().join()
                }

                instance.players.forEach { player -> player.teleport(center.add(0.0, 2.0, 0.0)) }
            }

            sender.sendMessage("<success>The spawn world was reset in ${timeTaken.format()}ms.".mm())
        }
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        sender.sendMessage("<p><em>This is an irreversible action!</em> <s>To reset the world, do <p>/resetspawn confirm<s>.".mm())
    }
}