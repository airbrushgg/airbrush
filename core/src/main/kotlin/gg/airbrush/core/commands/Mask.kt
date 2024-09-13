

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

import gg.airbrush.core.lib.Constants
import gg.airbrush.core.lib.PlayerDataCache
import gg.airbrush.core.lib.prettify
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player

class Mask : Command("mask") {
	init {
		defaultExecutor = CommandExecutor { sender, _ ->
			sender.sendMessage("<error>Usage: /mask <set/clear/view>".mm())
		}

		addSubcommand(SetSubcommand())
		addSubcommand(ClearSubcommand())
		addSubcommand(ViewSubcommand())
	}

	private class SetSubcommand : Command("set"), CommandExecutor {
		init {
			defaultExecutor = this
		}

		override fun apply(sender: CommandSender, context: CommandContext) {
			if(sender !is Player) return

			val targetPosition = sender.getTargetBlockPosition(Constants.EXTENDED_RANGE) ?: return
			val block = sender.instance.getBlock(targetPosition)
			PlayerDataCache.setBlockMask(sender.uuid, block)

			sender.sendMessage(Translations.translate("core.commands.mask.set", block.name().prettify()).mm())
		}
	}

	private class ClearSubcommand : Command("clear"), CommandExecutor {
		init {
			defaultExecutor = this
		}

		override fun apply(sender: CommandSender, context: CommandContext) {
			if(sender !is Player) return

			val mask = PlayerDataCache.getBlockMask(sender.uuid)

			if(mask == null) {
				sender.sendMessage(Translations.translate("core.commands.mask.none").mm())
				return
			}

			PlayerDataCache.clearBlockMask(sender.uuid)

			sender.sendMessage(Translations.translate("core.commands.mask.clear").mm())
		}
	}

	private class ViewSubcommand : Command("view"), CommandExecutor {
		init {
			defaultExecutor = this
		}

		override fun apply(sender: CommandSender, context: CommandContext) {
			if(sender !is Player) return
			val mask = PlayerDataCache.getBlockMask(sender.uuid)

			if(mask == null) {
				sender.sendMessage(Translations.translate("core.commands.mask.none").mm())
				return
			}

			sender.sendMessage(Translations.translate("core.commands.mask.view", mask.name().prettify()).mm())
		}
	}
}