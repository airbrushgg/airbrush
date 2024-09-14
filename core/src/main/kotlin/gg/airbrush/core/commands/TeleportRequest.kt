

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

import gg.airbrush.server.lib.mm
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

data class TeleportRequest(
    val requester: Player,
    val destination: Player
)
private val teleportRequests = ArrayList<TeleportRequest>()

class Tpa : Command("tpa"), CommandExecutor {
    private val destinationArg = ArgumentType.Entity("destination").onlyPlayers(true)

    init {
        defaultExecutor = this
        addSyntax(this, destinationArg)
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player) return

        if (!context.has(destinationArg)) {
            sender.sendMessage("<error>You must specify a player to teleport to".mm())
            return
        }

        val destination = context.get(destinationArg).findFirstPlayer(sender)
        if (destination == null) {
            sender.sendMessage("<error>That player is not online".mm())
            return
        }

        if (destination == sender) {
            sender.sendMessage("<error>You cannot teleport to yourself, silly!".mm())
            return
        }

        if (sender.instance != destination.instance) {
            sender.sendMessage("<error>You cannot send a request because ${destination.username} is in a different world".mm())
            return
        }

        val existingRequest = teleportRequests.find { it.requester == sender && it.destination == destination }
        if (existingRequest != null) {
            sender.sendMessage("<error>You already have a pending teleport request to ${destination.username}".mm())
            return
        }

        val request = TeleportRequest(sender, destination)
        teleportRequests.add(request)

        sender.sendMessage("<s>Sent a teleport request to <p>${destination.username}<s>!".mm())
        sender.playSound(Sound.sound(Key.key("minecraft:entity.arrow.shoot"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self())

        destination.sendMessage("<p>${sender.username} <s>has requested to teleport to you.\nType <p>/tpaccept <s>to accept the request, or <p>/tpdeny <s>to deny it.".mm())
        destination.playSound(Sound.sound(Key.key("minecraft:entity.arrow.hit"), Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self())
    }
}

class TpAccept : Command("tpaccept"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player) return

        val request = teleportRequests.firstOrNull { it.destination == sender }
        if (request == null) {
            sender.sendMessage("<error>You do not have any pending teleport requests".mm())
            return
        }

        if (!request.requester.isOnline) {
            // The requester is no longer online; remove the request and retry.
            teleportRequests.remove(request)
            return apply(sender, context)
        }

        teleportRequests.remove(request)
        request.requester.apply {
            sendMessage("<p>${sender.username} <s>has accepted your teleport request!".mm())
            if (request.destination.instance == instance) {
                teleport(request.destination.position)
            } else {
                sender.sendMessage("<error>Teleport failed! ${request.destination} is in a different world".mm())
            }
        }
    }
}

class TpDeny : Command("tpdeny"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player) return

        val request = teleportRequests.firstOrNull { it.destination == sender }
        if (request == null) {
            sender.sendMessage("<error>You do not have any pending teleport requests".mm())
            return
        }

        if (!request.requester.isOnline) {
            teleportRequests.remove(request)
            sender.sendMessage("<error>${request.requester.username} <s>is no longer online!".mm())
            return
        }

        teleportRequests.remove(request)
        request.requester.apply {
            sendMessage("<p>${sender.username} <s>has denied your teleport request!".mm())
        }
        sender.sendMessage("<s>You denied <p>${request.requester.username}<s>'s request!".mm())
    }
}