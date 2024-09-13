

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

import gg.airbrush.core.lib.*
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.color.Color
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.Player
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Stats : Command("statistics", "stats"), CommandExecutor {
    init {
        defaultExecutor = this
    }

    override fun apply(sender: CommandSender, context: CommandContext) {
        if (sender !is Player) return
        val sdkPlayer = SDK.players.get(sender.uuid)

        val levelColor = ColorUtil.oscillateHSV(Color(0xff0000), Color(0xff00ff), sdkPlayer.getLevel())
        val xpThreshold = sender.getXPThreshold()
        val exp = sdkPlayer.getExperience()
        val totalExpOverall = sdkPlayer.getLevel() * xpThreshold + exp
        val blocksCount = PlayerDataCache.blockCounts[sender.uuid] ?: 0
        val playerLocale = Locale.forLanguageTag(sender.settings.locale.replace("_", "-"))
        val joinDate = formatDateTime(Instant.ofEpochMilli(sdkPlayer.getData().firstJoin), playerLocale ?: Locale.US)
        val timePlayed = PlayerDataCache.getCurrentPlaytime(sender.uuid).toDuration(DurationUnit.MILLISECONDS)
        val topBlocks = SDK.pixels.getTopMaterials(sender.uuid)

        sender.sendMessage("""
            
            <green>Here are your stats on ${Translations.translate("core.scoreboard.title")}<reset><green>:
                <s>Level: <${TextColor.color(levelColor).asHexString()}>[${sdkPlayer.getLevel()}]
                <s>Experience: <p>${exp.format()}<s>/<p>${xpThreshold.format()} <s>(Total XP: <p>${totalExpOverall}<s>) 
                <s>Join Date: <p>${joinDate}
                <s>Playtime: <p>${timePlayed}
                <s>Total Blocks Painted: <p>${blocksCount.format()}
            
            <s>Top 3 Blocks:
                <s>1. <p>${topBlocks[0].first.name().prettify()} <s>(<p>${topBlocks[0].second.format()}<s>)
                <s>2. <p>${topBlocks[1].first.name().prettify()} <s>(<p>${topBlocks[1].second.format()}<s>)
                <s>3. <p>${topBlocks[2].first.name().prettify()} <s>(<p>${topBlocks[2].second.format()}<s>)
            
        """.trimIndent().mm())
    }

    private fun formatDateTime(instant: Instant, locale: Locale): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }
}