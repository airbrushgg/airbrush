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