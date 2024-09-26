

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

import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import net.minestom.server.coordinate.Pos
import net.minestom.server.tag.Tag
import java.io.File

object Constants {
    val schematicFolder = File("schematics")
    val worldFolder = File("canvases")
    val paintbrushName = Translations.translate("core.items.paintbrush").mm()
    val mainMenuName = Translations.translate("core.items.main_menu").mm()
    val paletteSelectorName = Translations.translate("core.items.palette_selector").mm()
	val eyedropperName = Translations.translate("core.items.eyedropper").mm()
    val eraserName = "Eraser".mm()

    // These are the NBT values for the items. Used to optimize the when-statement in BrushEvents.
    val airbrushToolTag = Tag.String("AirbrushTool")
    const val PAINTBRUSH_TOOL = "PAINTBRUSH"
    const val PALETTE_TOOL = "PALETTE"
    const val MAIN_MENU_TOOL = "MAIN_MENU"
    const val EYEDROPPER_TOOL = "EYEDROPPER"
    const val ERASER_TOOL = "ERASER"

    const val RANGE: Int = 20
    const val EXTENDED_RANGE: Int = 40

    val CENTER = Pos(0.0, 4.0, 0.0)
}