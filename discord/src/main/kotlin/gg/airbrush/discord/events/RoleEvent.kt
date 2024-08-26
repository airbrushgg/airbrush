package gg.airbrush.discord.events

import gg.airbrush.discord.discordConfig
import gg.airbrush.discord.lib.Logging
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.players.AirbrushPlayer
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*

object RoleEvent : ListenerAdapter() {
	private fun giveBoosters(player: AirbrushPlayer, role: String) {
		val twoX = SDK.boosters.getAvailableBoosters().find {
			it.id == "2x_fireworks"
		} ?: throw Exception("[giveBoosters] Failed to find 2x_fireworks")

		val threeX = SDK.boosters.getAvailableBoosters().find {
			it.id == "3x_solar_flare"
		} ?: throw Exception("[giveBoosters] Failed to find 3x_solar_flare")

		when(role) {
			"donor" -> player.addBooster(twoX)
			"superdonor" -> player.addBooster(threeX)
		}
	}

	override fun onGuildMemberRoleAdd(e: GuildMemberRoleAddEvent) {
		val member = e.member.id

		val receivedDonor = e.roles.find {
			it.id == discordConfig.donor
		}
		val receivedSuperDonor = e.roles.find {
			it.id == discordConfig.superdonor
		}

		if(receivedDonor == null && receivedSuperDonor == null)
			return

		val playerData = SDK.players.getByDiscordID(member)

		if(playerData == null) {
			val role = if(receivedSuperDonor == null) "⭐" else "⭐⭐"
			Logging.sendLog("Player with ID of $member has not linked their Discord account yet, and received [$role] role.")
			return
		}

		val role = if(receivedSuperDonor == null) "donor" else "superdonor"

		val sdkPlayer = SDK.players.get(UUID.fromString(playerData.uuid))
		val donatorRank = SDK.ranks.list().find {
			it.getData().name.lowercase() == role
		} ?: throw Exception("[RoleEvent > onGuildMemberRoleAdd] Failed to find donator rank for $role!")

		sdkPlayer.setRank(donatorRank.getData().name)

		giveBoosters(sdkPlayer, role)

		Logging.sendLog("""
			### Player was given ${if(role == "donor") "⭐" else "⭐⭐"}
			UUID: `${playerData.uuid}`
			Discord: <@$member> (`$member`)
		""".trimIndent())
	}

	override fun onGuildMemberRoleRemove(e: GuildMemberRoleRemoveEvent) {
		val member = e.user.id

		// If they have not removed super donor, then this returns.
		e.roles.find {
			it.id == discordConfig.superdonor
		} ?: return

		val playerData = SDK.players.getByDiscordID(member)

		if(playerData == null) {
			Logging.sendLog("Player with ID of $member has unlinked their Discord account, and their [⭐⭐] rank expired..")
			return
		}

		val sdkPlayer = SDK.players.get(UUID.fromString(playerData.uuid))

		// Fetches the default rank and sets the players rank to that

		val defaultRank = SDK.ranks.list().find {
			it.getData().name.lowercase() == "default"
		} ?: throw Exception("[RoleEvent > onGuildMemberRoleRemove] Failed to find default rank!")

		sdkPlayer.setRank(defaultRank.getData().name)
	}
}