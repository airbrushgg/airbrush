package gg.airbrush.sdk

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.airbrush.sdk.classes.boosters.Boosters
import gg.airbrush.sdk.classes.linking.Linking
import gg.airbrush.sdk.classes.palettes.Palettes
import gg.airbrush.sdk.classes.pixels.Pixels
import gg.airbrush.sdk.classes.players.Players
import gg.airbrush.sdk.classes.punishments.Punishments
import gg.airbrush.sdk.classes.ranks.Ranks
import gg.airbrush.sdk.classes.whitelist.Whitelist
import gg.airbrush.sdk.classes.worlds.Worlds
import gg.airbrush.sdk.lib.ConfigUtils

data class SDKConfig(val database: String, val isDev: Boolean, val whitelistEnabled: Boolean)
lateinit var config: SDKConfig

object SDK {
	private val mapper = tomlMapper {}

	init {
		val configPath = ConfigUtils.loadResource(
			clazz = Database::class.java,
			fileName = "config.toml",
			folder = "sdk"
		)
		config = mapper.decode(configPath)

		Database.load()
	}

	val ranks = Ranks()
	val worlds = Worlds()
	val pixels = Pixels()
	val players = Players()
	val linking = Linking()
	val palettes = Palettes()
	val whitelist = Whitelist()
	val punishments = Punishments()
	val boosters = Boosters()

	val isDev = config.isDev
}