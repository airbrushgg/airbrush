package gg.airbrush.core.lib

import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.worlds.WorldVisibility
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
    var worldName = "Spawn"

	if(player.instance.hasTag(Tag.String("CanvasUUID"))) {
		val canvasID = player.instance.getTag(Tag.String("CanvasUUID"))!!
		val world = SDK.worlds.getByUUID(canvasID)
			?: return Translations.translate("core.scoreboard.world", worldName).mm()
        worldName = when(world.data.visibility) {
            WorldVisibility.PRIVATE -> "Private World"
            WorldVisibility.PUBLIC -> "Public World"
        }
	}

    return Translations.translate("core.scoreboard.world", worldName).mm()
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