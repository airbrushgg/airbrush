

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

package gg.airbrush.core.events

import gg.airbrush.core.eventNode
import gg.airbrush.core.lib.ColorUtil
import gg.airbrush.core.lib.prettify
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.players.PaletteType
import gg.airbrush.sdk.events.LevelUpEvent
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.color.Color

class PlayerLevelUp {
    init {
        eventNode.addListener(
            LevelUpEvent::class.java
        ) { event: LevelUpEvent -> execute(event) }
    }

    private fun execute(event: LevelUpEvent) {
        val player = event.player
        val level = event.level

        val color = ColorUtil.oscillateHSV(Color(0xff0000), Color(0xff00ff), level)
	    val lvl = "<${TextColor.color(color).asHexString()}>[$level]"

        player.sendMessage(Translations.translate("core.leveling.level_up", lvl).mm())

        player.playSound(
            Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1.0f, 1.0f),
            Sound.Emitter.self()
        )

        if (level % 5 == 0) {
            val sdkPlayer = SDK.players.get(player.uuid)
            val radiusBeforeLevelUp = sdkPlayer.getData().brushRadius.max

	        when(level) {
		        5 -> sdkPlayer.setMaxRadius(2)
		        15 -> sdkPlayer.setMaxRadius(3)
		        25 -> sdkPlayer.setMaxRadius(4)
		        50 -> sdkPlayer.setMaxRadius(5)
            }

            val radiusAfterLevelUp = sdkPlayer.getData().brushRadius.max

            if(radiusAfterLevelUp > radiusBeforeLevelUp) {
                player.sendMessage(Translations.translate("core.leveling.new_max_radius", radiusBeforeLevelUp, radiusAfterLevelUp).mm())

                // TODO(cal): Figure out applicable sound for this.
                player.playSound(
                    Sound.sound(Key.key("block.note_block.bell"), Sound.Source.MASTER, 1.0f, 1.0f),
                    Sound.Emitter.self()
                )
            }

            val paletteOrdinal = sdkPlayer.getData().progressedPalette
            val paletteType = PaletteType.entries.firstOrNull {
                it.ordinal == paletteOrdinal
            } ?: return

            val progressed = sdkPlayer.progressPalette()
            if (!progressed) return

            // TODO: Move to a toast?
            val paletteName = paletteType.name.prettify().lowercase()
            player.sendMessage(Translations.translate("core.leveling.new_block", paletteName).mm())

            player.playSound(
                Sound.sound(Key.key("entity.player.levelup"), Sound.Source.MASTER, 1.0f, 1.0f),
                Sound.Emitter.self()
            )
        }
    }
}

