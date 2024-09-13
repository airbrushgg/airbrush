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

package gg.airbrush.sdk.classes.players

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import gg.airbrush.sdk.NotFoundException
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.ranks.AirbrushRank
import gg.airbrush.sdk.Database
import gg.airbrush.sdk.classes.boosters.BoosterData
import gg.airbrush.sdk.classes.palettes.Palettes
import gg.airbrush.sdk.classes.ranks.PermissionData
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

import java.util.UUID

@Serializable
data class ProgressionData(
    val paletteType: Int,
    val index: Int
)

@Serializable
data class RadiusInfo(
    var max: Int,
    var current: Int
)

data class PlayerData(
    val uuid: String,
    val firstJoin: Long,
    var timePlayed: Long = 0,
    var level: Int = 1,
    var experience: Int = 0,
    var blockCount: Long = 0,
    var rank: String,
    var palette: Int = 0,
    // The palette the player is progressing towards, defaults to wool.
    var progressedPalette: Int = 1,
    var brushRadius: RadiusInfo = RadiusInfo(
        max = 1,
        current = 1
    ),
    var chosenBlock: String = "minecraft:black_concrete",
    var paletteProgression: List<ProgressionData>,
    var discordId: Long? = null,
    var ownedBoosters: List<BoosterData>,
	var pronouns: String? = null
)

@Suppress("unused")
enum class PaletteType {
    CONCRETE,
    WOOL,
    CONCRETE_POWDER,
    TERRACOTTA,
    GLAZED_TERRACOTTA
}

private val db = Database.get()

class AirbrushPlayer(uuid: UUID) {
    private val col = db.getCollection<PlayerData>("players")
    private val query = Filters.eq(PlayerData::uuid.name, uuid.toString())
    private val data: PlayerData

    init {
        data = col.find(query).firstOrNull()
            ?: throw NotFoundException("Player with UUID of $uuid not found.")
    }

    @Suppress("unused")
    fun getData(): PlayerData {
        return data
    }

    @Suppress("unused")
    fun getLevel(): Int {
        return data.level
    }

    @Suppress("unused")
    fun setLevel(level: Int) {
        col.updateOne(query, Updates.set(PlayerData::level.name, level))
        data.level = level
    }

	@Suppress("unused")
	fun incrementBlockCount() = runBlocking<Unit> {
		val newCount = data.blockCount + 1
		data.blockCount = newCount
        launch { col.updateOne(query, Updates.set(PlayerData::blockCount.name, newCount)) }
	}

	@Suppress("unused")
	fun setPronouns(pronouns: String) {
		col.updateOne(query, Updates.set(PlayerData::pronouns.name, pronouns))
		data.pronouns = pronouns
	}

	@Suppress("unused")
	fun setProgressionPalette(type: PaletteType) {
		col.updateOne(query, Updates.set(PlayerData::progressedPalette.name, type.ordinal))
		data.progressedPalette = type.ordinal
	}

    @Suppress("unused")
    fun setRadius(radius: Int, force: Boolean = false) {
        if((radius < 1 || radius > 5) && !force)
            throw Exception("Invalid radius. Radii must be between 1 and 5.")

        data.brushRadius.current = radius

        col.updateOne(query, Updates.set(PlayerData::brushRadius.name, data.brushRadius))
    }

    @Suppress("unused")
    fun setMaxRadius(radius: Int) {
        if(radius < 1 || radius > 5)
            throw Exception("Invalid radius. Radii must be between 1 and 5.")

        data.brushRadius.max = radius

        col.updateOne(query, Updates.set(PlayerData::brushRadius.name, data.brushRadius))
    }

    @Suppress("unused")
    fun getExperience(): Int {
        return data.experience
    }

    @Suppress("unused")
    fun setExperience(experience: Int) = runBlocking<Unit> {
        data.experience = experience
        launch { col.updateOne(query, Updates.set(PlayerData::experience.name, experience)) }
    }

	@Suppress("unused")
	fun setDiscordId(discordId: Long) {
		col.updateOne(query, Updates.set(PlayerData::discordId.name, discordId))
		data.discordId = discordId
	}

	@Suppress("unused")
	fun wipeDiscordId() {
		val query = Filters.eq(PlayerData::discordId.name, data.discordId)
		col.updateOne(query, Updates.unset(PlayerData::discordId.name))
	}

    @Suppress("unused")
    fun getRank(): AirbrushRank {
        return SDK.ranks.get(UUID.fromString(data.rank))
    }

    @Suppress("unused")
    fun setRank(name: String) {
        val rankExists = SDK.ranks.exists(name)

        if(!rankExists) {
            throw NotFoundException("Cannot grant '$name' to ${data.uuid}.")
        }

        val rankData = SDK.ranks.get(name)
        val rankId = rankData.getData().id

        col.updateOne(query, Updates.set(PlayerData::rank.name, rankId))

        data.rank = rankId
    }

    @Suppress("unused")
    fun setPalette(type: PaletteType) {
        col.updateOne(query, Updates.set(PlayerData::palette.name, type.ordinal))
        data.palette = type.ordinal

        val hasProgression = getPaletteProgression(type)

        // only add progression data if they don't have it
        if(hasProgression != null) return

        val list = data.paletteProgression.toMutableList()
        list.add(
            ProgressionData(
                paletteType = type.ordinal,
                index = 1
            )
        )

        col.updateOne(query, Updates.set(PlayerData::paletteProgression.name, list))
    }

    // If this method returns null, then the player does not own the palette.
    @Suppress("unused")
    fun getPaletteProgression(type: PaletteType): ProgressionData? {
        val progressionInfo = data.paletteProgression.find {
            it.paletteType == type.ordinal
        }

        return progressionInfo
    }

    // If this method returns null, then the player does not own the palette.
    @Suppress("unused")
    fun progressPalette(): Boolean {
	    val currentPalette = PaletteType.entries[data.progressedPalette]

        val progressionInfo = data.paletteProgression.find {
            it.paletteType == currentPalette.ordinal
        }!!

        val paletteSize = Palettes().get(currentPalette).size
        val newIndex = progressionInfo.index + 1

        if (newIndex >= paletteSize) {
            // player has reached the end of palette progression for this palette
            return false
        }

        // update the progression index in the data object
        data.paletteProgression = data.paletteProgression.map {
            if (it.paletteType == currentPalette.ordinal) {
                it.copy(index = newIndex)
            } else {
                it
            }
        }

        col.updateOne(query, Updates.set(PlayerData::paletteProgression.name, data.paletteProgression))

        return true
    }

    @Suppress("unused")
    fun setChosenBlock(block: String) {
        col.updateOne(query, Updates.set(PlayerData::chosenBlock.name, block))
        data.chosenBlock = block
    }

    @Suppress("unused")
    fun hasPermission(node: PermissionData): Boolean {
        val rankUUID = UUID.fromString(data.rank)
        val playerRank = SDK.ranks.get(rankUUID)
        val rankPermissions = playerRank.getData().permissions
        return rankPermissions.contains(node)
    }

    @Suppress("unused")
    fun addBooster(booster: BoosterData) {
        data.ownedBoosters = data.ownedBoosters.plus(booster)
        col.updateOne(query, Updates.addToSet(PlayerData::ownedBoosters.name, booster))
    }

    @Suppress("unused")
    fun removeBooster(booster: BoosterData) {
        val newList = data.ownedBoosters.toMutableList()
        if (newList.remove(booster)) {
            data.ownedBoosters = newList
            col.updateOne(query, Updates.set(PlayerData::ownedBoosters.name, data.ownedBoosters))
        }
    }

    @Suppress("unused")
    fun getOwnedBoosters(): List<BoosterData> {
        return data.ownedBoosters
    }

    @Suppress("unused")
    fun addPlaytime(duration: Long) {
        data.timePlayed += duration
        col.updateOne(query, Updates.set(PlayerData::timePlayed.name, data.timePlayed))
    }
}