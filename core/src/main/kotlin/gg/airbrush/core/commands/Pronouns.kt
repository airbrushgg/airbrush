package gg.airbrush.core.commands

import gg.airbrush.sdk.SDK
import gg.airbrush.server.lib.mm
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class Pronouns : Command("pronouns") {
	init {
		defaultExecutor = CommandExecutor { sender, _ ->
			sender.sendMessage("<error>Usage: /pronouns <get/set>".mm())
		}

		addSubcommand(GetSubcommand())
		addSubcommand(SetSubcommand())
	}

	private class GetSubcommand : Command("get"), CommandExecutor {
		private val playerArgument = ArgumentType.String("player")
		init {
			defaultExecutor = this
			addSyntax(this, playerArgument)
		}

		override fun apply(sender: CommandSender, context: CommandContext) {
			if (!context.has(playerArgument)) {
				sender.sendMessage("<error>Usage: /pronouns get <player>".mm())
				return
			}

			val selectedPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(context.get(playerArgument))

			if(selectedPlayer == null) {
				sender.sendMessage("<error>That player is not online".mm())
				return
			}

			val player = SDK.players.get(selectedPlayer.uuid)
			val pronouns = player.getData().pronouns

			if(pronouns == null) {
				sender.sendMessage("<error>That player has not set any pronouns :(".mm())
				return
			}

			sender.sendMessage("<p>Their pronouns are <s>$pronouns</s>.".mm())
		}
	}

	private class SetSubcommand : Command("set"), CommandExecutor {
		private val pronounArgument = ArgumentType.StringArray("pronouns")

		init {
			defaultExecutor = this
			addSyntax(this, pronounArgument)
		}

		override fun apply(sender: CommandSender, context: CommandContext) {
			if (!context.has(pronounArgument)) {
				sender.sendMessage("<error>Usage: /pronouns set <pronouns>".mm())
				return
			}

			val pronouns = context.get(pronounArgument).joinToString(" ") { it }
			val pronounPattern = Regex("^(he|him|his|she|her|hers|they|them|their|theirs)\\/(he|him|his|she|her|hers|they|them|their|theirs)$")
			val proper = pronounPattern.matches(pronouns)

			if(!proper) {
				sender.sendMessage("<error>These pronouns aren't recognized :(".mm())
				return
			}

			SDK.players.get((sender as Player).uuid).setPronouns(pronouns)

			sender.sendMessage("<success>Successfully set your pronouns to <b>$pronouns</b>.".mm())
		}
	}
}