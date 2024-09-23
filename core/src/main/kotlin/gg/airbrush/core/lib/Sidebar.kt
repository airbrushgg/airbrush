

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

package gg.airbrush.core.lib

import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.color.Color
import net.minestom.server.entity.Player
import net.minestom.server.tag.Tag

fun getLevelLine(player: Player): Component {
    val levelColor = ColorUtil.oscillateHSV(Color(0xff0000), Color(0xff00ff), player.level)
    val colorHex = TextColor.color(levelColor).asHexString()
	val levelText = "<$colorHex>[${player.level}]"

    return Translations.translate("core.scoreboard.level", levelText).mm()
}

fun getWorldLine(player: Player): Component {
    val instance = player.instance

    val internalWorldName = instance.getTag(Tag.String("PersistentWorld")) ?: null
    val canvasId = instance.getTag(Tag.String("CanvasUUID")) ?: null

    fun getWorldName(): String {
        if (internalWorldName == null && canvasId == null) {
            return "Spawn"
        }

        if (internalWorldName == "star_world") {
            return "<y>★"
        }

        if (canvasId != null) {
            val world = SDK.worlds.getByUUID(canvasId) ?: return "Unknown World"
            return world.data.name
        }

        return "Unknown World"
    }

    return Translations.translate("core.scoreboard.world", getWorldName()).mm()
}

// This is going to be removed for now as updating it would suck.
//fun getPlaytimeLine(player: Player): Component {
//    return "<s>Playtime: <p>0D</p>:<p>0H</p>:<p>0M".mm() // todo: track playtime
//}

fun getRankLine(player: Player): Component {
    val sdkPlayer = SDK.players.get(player.uuid)
    val rank = sdkPlayer.getRank().getData()
    val rankColor = (rank.prefix.mm().color() ?: TextColor.color(200, 130, 224)).asHexString()
    val rankName = "<$rankColor>${rank.name}"
    return Translations.translate("core.scoreboard.rank", rankName).mm()
}

fun getBlocksLine(player: Player): Component {
    val localCount = PlayerDataCache.blockCounts[player.uuid] ?: 0
    return "<s>Blocks: <p>${localCount.format()}".mm()
}

fun getXPLine(player: Player): Component {
    val sdkPlayer = SDK.players.get(player.uuid)
    val xpThreshold = player.getXPThreshold()
	val exp = sdkPlayer.getExperience()

    return Translations.translate("core.scoreboard.exp", exp.format(), xpThreshold.format()).mm()
}