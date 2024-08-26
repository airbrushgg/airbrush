package gg.airbrush.core.lib

import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
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
}