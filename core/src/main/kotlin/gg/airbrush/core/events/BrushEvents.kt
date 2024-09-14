

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

import Boost
import gg.airbrush.core.commands.mainmenu.openMainMenu
import gg.airbrush.core.commands.mainmenu.openPaletteGUI
import gg.airbrush.core.commands.openBrushGUI
import gg.airbrush.core.lib.*
import gg.airbrush.sdk.NotFoundException
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.players.PaletteType
import gg.airbrush.sdk.events.LevelUpEvent
import gg.airbrush.sdk.lib.Translations
import gg.airbrush.server.lib.mm
import gg.airbrush.worlds.WorldManager
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.batch.AbsoluteBlockBatch
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material
import net.minestom.server.tag.Tag
import net.minestom.server.timer.TaskSchedule
import java.util.*
import kotlin.math.roundToInt

private class PixelManager {
    private val countMap = HashMap<UUID, Int>()

    init {
        MinecraftServer.getSchedulerManager().scheduleTask({
            if (countMap.isEmpty()) return@scheduleTask
            synchronized(countMap) {
                SDK.players.updateBlockCounts(countMap)
                countMap.clear()
            }
        }, TaskSchedule.immediate(), TaskSchedule.seconds(5))
    }

    fun updateBlockCount(player: UUID, count: Int) {
        if (!countMap.containsKey(player)) {
            countMap[player] = count
            return
        }
        countMap[player] = countMap[player]!! + count
    }
}

class BrushEvents {
    private val pixels = PixelManager()

    init {
        val eventHandler = EventNode.event("PaintableWorlds", EventFilter.INSTANCE) {
            it.instance == WorldManager.defaultInstance ||
            it.instance.getTag(Tag.String("PersistentWorld")) == "donator_world" ||
            it.instance.hasTag(Tag.String("CanvasUUID"))
        }
        MinecraftServer.getGlobalEventHandler().addChild(eventHandler)

        eventHandler.addListener(
            PlayerUseItemEvent ::class.java
        ) { event: PlayerUseItemEvent -> execute(event) }

        eventHandler.addListener(
            PlayerHandAnimationEvent ::class.java
        ) { event: PlayerHandAnimationEvent -> brushGUI(event) }

        eventHandler.addListener(
            ItemDropEvent ::class.java
        ) { event: ItemDropEvent -> event.isCancelled = true }
    }

    private fun execute(event: PlayerUseItemEvent) {
        val player = event.player
        val toolType = player.itemInMainHand.getTag(Constants.airbrushToolTag) ?: return
        when (toolType) {
            Constants.MAIN_MENU_TOOL -> return openMainMenu(player)
            Constants.PALETTE_TOOL -> return openPaletteGUI(player)
            Constants.EYEDROPPER_TOOL -> return useEyedropperTool(player)
        }

        val isPaintbrush = toolType == Constants.PAINTBRUSH_TOOL
        val isEraser = toolType == Constants.ERASER_TOOL
        if (!isPaintbrush && !isEraser) return

        val instance = player.instance
        val isCustomCanvas = instance.hasTag(Tag.String("CanvasUUID"))
        if (isCustomCanvas) {
            val canvasID = instance.getTag(Tag.String("CanvasUUID"))!!
            val world = SDK.worlds.getByUUID(canvasID) ?: return
            if (player.uuid.toString() != world.data.ownedBy) {
                player.sendActionBar("<error>You cannot paint in this world!".mm())
                return
            }
        }

        val sdkPlayer = SDK.players.get(player.uuid)

        val chosenBlock = if (isPaintbrush) {
            Block.fromNamespaceId(sdkPlayer.getData().chosenBlock) ?: return
        } else {
            Material.WHITE_CONCRETE.block()
        }

        val targetPosition = player.getTargetBlockPosition(Constants.RANGE) ?: return
	    val currentMask = PlayerDataCache.getBlockMask(player.uuid)

	    val brushRadius = sdkPlayer.getData().brushRadius.current
        val blocksToPaint = fillSphere(targetPosition, brushRadius)
            .filterNot { pos ->
                // Filter out any blocks that should not get painted.
                val block = instance.getBlock(pos)
	            val exclusions = block.isAir
                        || block.compare(chosenBlock)
                        || block.compare(Material.BARRIER.block())
                        // TODO(cal): Improve this with potential region system?
                        || pos.y() == 3.0
	            val mask = (currentMask != null) && !block.compare(currentMask)
	            mask || exclusions
            }

        // If there are no blocks to paint, return early.
        if (blocksToPaint.isEmpty()) {
            return
        }

        // Apply the block changes to the world.
        val batch = AbsoluteBlockBatch()
        for (pos in blocksToPaint) {
            batch.setBlock(pos, chosenBlock)
        }
        batch.apply(instance, null)

        // Add the pixels to the database.
        SDK.pixels.paintMulti(blocksToPaint, player.uuid, chosenBlock.registry().material()!!)

        // Update the local block count.
        PlayerDataCache.incrementBlockCount(player.uuid, blocksToPaint.size)
        pixels.updateBlockCount(player.uuid, blocksToPaint.size) // Update block count as well.

	    // Alert the player a mask is enabled
	    if (currentMask !== null) {
			player.sendActionBar(Translations.translate("core.commands.mask.alert", currentMask.name().prettify()).mm())
		}

        // Don't award any experience if using the eraser.
        if (isEraser) {
            return
        }

	    // Only update EXP and Level if their brush radius is less than or equal to five.
	    if (brushRadius <= 5) {
		    val xp = sdkPlayer.getExperience() + (1 * Boost.getMultiplier()).roundToInt()
		    val xpThreshold = player.getXPThreshold()
		    sdkPlayer.setExperience(xp)

		    if (xp >= xpThreshold) {
			    sdkPlayer.setExperience(xp - xpThreshold)
			    sdkPlayer.setLevel(sdkPlayer.getLevel() + 1)
			    EventDispatcher.call(LevelUpEvent(player, sdkPlayer.getLevel()))
		    }

		    player.level = sdkPlayer.getLevel()
		    player.exp = sdkPlayer.getExperience().toFloat() / xpThreshold
		}

        val sidebar = sidebars[player.uuid] ?: return
        sidebar.updateLineContent("level", getLevelLine(player))
        sidebar.updateLineContent("blocks", getBlocksLine(player))
        sidebar.updateLineContent("xp", getXPLine(player))
    }

    private fun useEyedropperTool(player: Player) {
        val sdkPlayer = SDK.players.get(player.uuid)
        val targetPosition = player.getTargetBlockPosition(Constants.EXTENDED_RANGE) ?: return
        val block = player.instance.getBlock(targetPosition)

        val ownedBlocks = mutableListOf<String>()
        PaletteType.entries.forEach { paletteType ->
            val progression = sdkPlayer.getPaletteProgression(paletteType)
                ?: throw Exception("Failed to fetch progression for ${player.uuid}.")
            val owned = SDK.palettes.getBlocks(paletteType, progression.index)
            ownedBlocks.addAll(owned)
        }

        val isOwned = ownedBlocks.find {
            val b = it.toMaterial()
                ?: throw NotFoundException("$it is an invalid material.")
            block.name() == b.block().name()
        }

        if(isOwned == null) {
            player.sendActionBar("<error>You have not unlocked this block!".mm())
            return
        }

        sdkPlayer.setChosenBlock(block.name())
        player.sendActionBar("<success>Set your block to ${block.name().prettify()}!".mm())
    }

    private fun brushGUI(event: PlayerHandAnimationEvent) {
        val player = event.player
        val toolType = player.itemInMainHand.getTag(Tag.String("AirbrushTool")) ?: return
        when (toolType) {
            Constants.MAIN_MENU_TOOL -> return openMainMenu(player)
            Constants.PALETTE_TOOL -> return openPaletteGUI(player)
            Constants.PAINTBRUSH_TOOL -> return openBrushGUI(player)
        }
    }

    private fun fillSphere(origin: Point, radius: Int): List<Point> {
        if (radius == 1) {
            return listOf(origin)
        }

        val blocksToPaint = ArrayList<Point>()
        val originX = origin.blockX()
        val originY = origin.blockY()
        val originZ = origin.blockZ()

        for (x in originX-radius..<originX+radius) {
            for (z in originZ-radius..<originZ+radius) {
                for (y in originY-radius..<originY+radius) {
                    val dist = origin.distance(x.toDouble(), y.toDouble(), z.toDouble())
                    if (dist >= radius * 0.65)
                        continue
                    blocksToPaint += Pos(x.toDouble(), y.toDouble(), z.toDouble())
                }
            }
        }

        return blocksToPaint
    }
}