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

package gg.airbrush.punishments.commands

import gg.airbrush.pocket.GUI
import gg.airbrush.punishments.lib.convertDate
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.executeCommand
import gg.airbrush.sdk.lib.fetchInput
import gg.airbrush.sdk.lib.isConfirmed
import gg.airbrush.server.lib.mm
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.ClickType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.util.*

class EditPunishmentCommand : Command("editpunishment") {
    init {
        setCondition { sender, _ ->
            sender.hasPermission("core.staff")
        }

        defaultExecutor = CommandExecutor { sender, _ ->
            sender.sendMessage("<s>Invalid usage.".mm())
        }

        addSyntax(this::apply, ArgumentType.UUID("punishment"))
    }

    private fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player) return

        val punishmentId = context.get<UUID>("punishment")

        val punishmentExists = SDK.punishments.exists(punishmentId)

        if (!punishmentExists) {
            sender.sendMessage("<error>A punishment with that ID does not exist!".mm())
            return
        }

        val punishment = SDK.punishments.get(punishmentId)

        val template = """
            xxxxxxxxx
            xaxbxcxdx
            xxxxxxxxx
        """.trimIndent()
        val gui = GUI(template, "Edit Punishment", InventoryType.CHEST_3_ROW)

        val borderItem = ItemStack.builder(Material.PURPLE_STAINED_GLASS_PANE)
            .customName("<reset>".mm())
            .build()
        gui.put('x', borderItem)

        val revertItem = ItemStack.builder(Material.BARRIER)
            .customName("<p>Revert".mm())
            .lore("<s>Revert this punishment.".mm())
            .build()
        gui.put('a', revertItem) {
            if(punishment.data.reverted != null) {
                sender.sendMessage("<error>This punishment has already been reverted!".mm())
                return@put
            }

            it.player.closeInventory()
            it.player.executeCommand("revertpun $punishmentId")
        }

        val reasonInput = fetchInput(sender, bypassFilter = true) {
            punishment.setReason(it)
            sender.sendMessage("<success>Successfully edited reason.".mm())
        }

        val editReasonItem = ItemStack.builder(Material.PAPER)
            .customName("<p>Edit reason".mm())
            .lore("<s>Edit the reason for this punishment.".mm())
            .build()
        gui.put('b', editReasonItem) {
            sender.sendMessage("<s>Enter the new reason below:".mm())
            it.player.closeInventory()
            reasonInput.prompt()
        }

        val notesItem = ItemStack.builder(Material.BOOK)
            .customName("<p>Edit notes".mm())
            .lore(listOf(
                "<s>Edit the notes for this punishment.".mm(),
                Component.empty(),
                "<s>➜ Left click to add a note".mm(),
                "<s>➜ Right click to clear".mm()
            ))
            .build()

        val durationInput = fetchInput(sender) {
            try {
                val duration = convertDate(it)
                punishment.setDuration(duration)
                sender.sendMessage("<success>Successfully edited duration to $it.".mm())
            } catch (e: Exception) {
                sender.sendMessage("<error>Invalid duration specified, cancelling.".mm())
                return@fetchInput
            }
        }

        val durationItem = ItemStack.builder(Material.CLOCK)
            .customName("<p>Edit duration".mm())
            .lore("<s>Edit the duration for this punishment.".mm())
            .build()
        gui.put('c', durationItem) {
            sender.sendMessage("<s>Enter the new duration below (ex: 1h):".mm())
            it.player.closeInventory()
            durationInput.prompt()
        }

        val notesInput = fetchInput(sender, bypassFilter = true) {
            if(punishment.data.notes.isNullOrEmpty()) {
                punishment.setNotes(it)
            } else {
                punishment.setNotes("${punishment.data.notes}\n\n$it")
            }

            sender.sendMessage("<success>Successfully edited notes.".mm())
        }

        val confirmNoteWipeInput = fetchInput(sender) {
            if(!sender.hasPermission("core.admin")) {
                sender.sendMessage("<error>You do not have permission to clear notes.".mm())
                return@fetchInput
            }

            if (!isConfirmed(it)) {
                sender.sendMessage("<error>Cancelled clearing notes.".mm())
                return@fetchInput
            }

            punishment.setNotes(null)
            sender.sendMessage("<success>Successfully cleared notes.".mm())
        }

        gui.put('d', notesItem) {
            if(it.clickType == ClickType.LEFT_CLICK) {
                sender.sendMessage("<s>Enter your note below:".mm())
                it.player.closeInventory()
                notesInput.prompt()
                return@put
            }

            if(it.clickType == ClickType.RIGHT_CLICK) {
                it.player.closeInventory()
                sender.sendMessage("<s>Please enter <g>yes</g> to confirm:".mm())
                confirmNoteWipeInput.prompt()
            }
        }

        gui.open(sender)
    }
}