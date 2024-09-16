

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

package gg.airbrush.core.commands.history

import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@Suppress("unused")
data class RollbackRequest(
    var centerX: Int,
    var centerY: Int,
    var radius: Int,
)

class Rollback : Command("rollback", "rb"), CommandExecutor {
    //private val welcomeSound = Sound.sound(Key.key("block.note_block.bell"), Sound.Source.MASTER, 1f, 1f)

    private val radiusArgument = ArgumentType.Integer("radius")
    private val timeArgument = ArgumentType.StringArray("time")

    init {
        defaultExecutor = CommandExecutor { sender, _ ->
            sender.sendMessage("<error>Usage: /rollback <radius> <time>".mm())
        }
        setCondition { sender, _ -> sender.hasPermission("core.admin") }
        addSyntax(this, radiusArgument, timeArgument)
        //addSubcommand(SaveSubcommand())
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player) return

        val timeString = context.get(timeArgument).joinToString(" ")
        val duration = Duration.parseOrNull(timeString) ?: run {
            sender.sendMessage("<error>Invalid time format. (Hint: Make sure to split units by spaces!)".mm())
            return
        }

        val timestamp = Instant.now().minus(duration.toJavaDuration()).toEpochMilli()
        val positions = getSphere(sender.position, context.get(radiusArgument))

        val instance = sender.instance
        for (position in positions) {
            val blockAtPos = instance.getBlock(position)
            if (blockAtPos.isAir) continue

            // Dear god, I hope no one runs this with a radius of like greater than 2.
//            val pixelData = SDK.pixels.getPixelDataByPos(position)
//                .toList()
//                .flatMap { it.pixels }
//                .filter { it.timestamp <= timestamp }
//                .maxByOrNull { it.timestamp } ?: continue
//            instance.setBlock(position, pixelData.from().material.block())
        }

        sender.sendMessage("<success> Rolling back to $duration ago".mm())

//        if (isViewingRollback(sender)) {
//            sender.sendMessage("<error>You are already viewing the rollback viewer.".mm())
//            return
//        }
//
//        val previousPosition = sender.position
//        sender.setInstance(rollbackWorld, previousPosition).thenRun {
//            sender.sendPacket(InitializeWorldBorderPacket(
//                sender.position.x, sender.position.z, 10.0, 10.0, 0, 0, 0, 0))
//
//            pendingRequests += sender.uuid to RollbackRequest(
//                centerX = sender.position.blockX(),
//                centerY = sender.position.blockY(),
//                radius = 10
//            )
//
//            // Save the player's inventory so that we can restore it later.
//            savedInventory[sender.uuid] = sender.inventory.itemStacks
//            sender.inventory.clear()
//
//            // Give the player tools to use in the rollback viewer.
//            sender.inventory.setItemStack(0, ItemStack.builder(Material.STICK)
//                .displayName("Select Region".mm())
//                .set(Tag.String("RollbackToolType"), "SelectRegion")
//                .build())
//            sender.inventory.setItemStack(1, ItemStack.builder(Material.ARROW)
//                .displayName("Set Center".mm())
//                .set(Tag.String("RollbackToolType"), "SetCenter")
//                .build())
//            sender.inventory.setItemStack(2, ItemStack.builder(Material.WOODEN_SHOVEL)
//                .displayName("Change Radius".mm())
//                .set(Tag.String("RollbackToolType"), "ChangeRadius")
//                .build())
//            sender.inventory.setItemStack(8, ItemStack.builder(Material.BOOK)
//                .displayName("Confirm/Cancel Changes".mm())
//                .set(Tag.String("RollbackToolType"), "ConfirmChanges")
//                .build())
//            sender.inventory.update()
//
//            sender.sendMessage("""
//                <p>You have entered the /rollback viewer.</p>
//                <s>Changes you make here will not be saved unless confirmed.</s>
//                <s>Careful! Changes that have been confirmed cannot be reversed!</s>
//            """.trimIndent().mm())
//            sender.playSound(welcomeSound, Sound.Emitter.self())
//        }
    }

    private fun getSphere(origin: Pos, radius: Int): List<Pos> {
        val blocksToRollback = ArrayList<Pos>()
        val originX = origin.blockX()
        val originY = origin.blockY()
        val originZ = origin.blockZ()
        for (x in originX-radius..<originX+radius) {
            for (z in originZ-radius..<originZ+radius) {
                for (y in originY-radius..<originY+radius) {
                    val dist = origin.distance(x.toDouble(), y.toDouble(), z.toDouble())
                    if (dist >= radius)
                        continue
                    blocksToRollback += Pos(x.toDouble(), y.toDouble(), z.toDouble())
                }
            }
        }
        return blocksToRollback
    }

//    private class SaveSubcommand : Command("save"), CommandExecutor {
//        init {
//            defaultExecutor = this
//        }
//
//        override fun apply(sender: CommandSender, context: CommandContext) {
//            if (sender !is Player) return
//
//            if (!isViewingRollback(sender)) {
//                sender.sendMessage("<error>You are not currently viewing the rollback viewer.".mm())
//                return
//            }
//
//            showConfirmationDialog(sender)
//        }
//    }

//    companion object {
//        private val savedInventory = HashMap<UUID, Array<ItemStack>>()
//        private val pendingRequests = HashMap<UUID, RollbackRequest>()
//        private val rollbackWorld = WorldManager.createFromTemplate("rollback_world").apply {
//            eventNode().addListener(PlayerHandAnimationEvent::class.java, ::handlePlayerHandAnimation)
//            eventNode().addListener(PlayerUseItemEvent::class.java, ::handlePlayerUseItem)
//            eventNode().addListener(InventoryCloseEvent::class.java, ::handleInventoryClose)
//        }
//
//        fun isViewingRollback(player: Player): Boolean {
//            return player.instance == rollbackWorld
//        }
//
//        private fun handlePlayerHandAnimation(event: PlayerHandAnimationEvent) {
//            val toolType = event.player.itemInMainHand.getTag(Tag.String("RollbackToolType")) ?: return
//            val player = event.player
//            val currentRequest = pendingRequests[player.uuid]!!
//
//            when (toolType) {
//                "ChangeRadius" -> {
//                    val newRadius = currentRequest.radius--
//                    player.sendPacket(WorldBorderSizePacket(newRadius.toDouble()))
//                    player.sendMessage("<success>Set radius to $newRadius".mm())
//                }
//            }
//        }
//
//        private fun handlePlayerUseItem(event: PlayerUseItemEvent) {
//            val toolType = event.itemStack.getTag(Tag.String("RollbackToolType")) ?: return
//            val player = event.player
//            val currentRequest = pendingRequests[player.uuid]!!
//
//            when (toolType) {
//                "SelectRegion" -> {
//                    player.sendMessage("<error>TODO: This tool is not implemented yet.".mm())
//                }
//                "SetCenter" -> {
//                    val position = event.player.position
//                    player.sendPacket(WorldBorderCenterPacket(position.x(), position.z()))
//                    player.sendMessage("<success>Set center to ${position.x()}, ${position.z()}".mm())
//                }
//                "ChangeRadius" -> {
//                    val newRadius = currentRequest.radius++
//                    player.sendPacket(WorldBorderSizePacket(newRadius.toDouble()))
//                    player.sendMessage("<success>Set radius to $newRadius".mm())
//                }
//                "ConfirmChanges" -> {
//                    showConfirmationDialog(player)
//                }
//            }
//        }
//
//        private fun handleInventoryClose(event: InventoryCloseEvent) {
//            val player = event.player
//            if (player.itemInOffHand.material() != Material.BOOK) return
//
//            val currentRequest = pendingRequests[player.uuid]!!
//
//            val choice = player.getTag(Tag.String("ConfirmationChoice")) ?: "Cancel"
//            val feedbackText: String
//
//            if (choice == "Confirm") {
//                player.sendMessage("TODO: Rollback")
//                player.sendMessage(currentRequest.toString())
//
//                feedbackText = "<success>Your changes have been applied!</success>"
//            } else {
//                feedbackText = "<#c70000>Your changes have been cancelled.</#c70000>"
//            }
//
//            pendingRequests.remove(player.uuid)
//
//            val itemStacks = savedInventory.remove(player.uuid) ?: emptyArray()
//            player.inventory.clear()
//            player.inventory.addItemStacks(itemStacks.toList(), TransactionOption.ALL)
//            player.inventory.update()
//
//            player.setInstance(WorldManager.defaultInstance, player.position).thenRun {
//                player.sendMessage(feedbackText.mm())
//            }
//        }
//    }
}

//private fun showConfirmationDialog(player: Player) {
//    val descriptionText = """
//        Are you sure you want to confirm these changes?
//
//        <error>Warning - This action cannot be reversed!
//    """.trimIndent().mm()
//
//    val confirmButton = "<#19911d>[Confirm Changes]</#19911d>".mm()
//        .clickEvent(ClickEvent.callback {
//            player.setTag(Tag.String("ConfirmationChoice"), "Confirm")
//        })
//    val cancelButton = "<#c70000>[Cancel Changes]</#c70000>".mm()
//        .clickEvent(ClickEvent.callback {
//            player.setTag(Tag.String("ConfirmationChoice"), "Cancel")
//        })
//
//    val book = Book.builder()
//    book.addPage(descriptionText
//        .appendNewline()
//        .append(confirmButton)
//        .appendNewline()
//        .append(cancelButton)
//    )
//    player.openBook(book.build())
//}