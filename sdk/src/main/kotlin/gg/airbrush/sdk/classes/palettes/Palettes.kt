package gg.airbrush.sdk.classes.palettes

import cc.ekblad.toml.decode
import cc.ekblad.toml.tomlMapper
import gg.airbrush.sdk.SDK
import gg.airbrush.sdk.classes.players.PaletteType
import kotlinx.serialization.Serializable

class Palettes {
    private val mapper = tomlMapper {}

    @Serializable
    data class Palette(
        val concrete: List<String>,
        val concretePowder: List<String>,
        val wool: List<String>,
        val terracotta: List<String>,
        val glazedTerracotta: List<String>
    )

    private var config: Palette;

    init {
        val text = SDK::class.java.getResourceAsStream("/palette.toml")?.bufferedReader().use { it?.readText() }
            ?: throw Exception("AAAAAAAAA")
        config = mapper.decode<Palette>(text)
    }

    // Returns all blocks of a specific palette type
    fun get(type: PaletteType): List<String> {
        return when(type) {
            PaletteType.CONCRETE -> config.concrete
            PaletteType.CONCRETE_POWDER -> config.concretePowder
            PaletteType.WOOL -> config.wool
            PaletteType.TERRACOTTA -> config.terracotta
            PaletteType.GLAZED_TERRACOTTA -> config.glazedTerracotta
        }
    }

    // Returns all the unlocked blocks provided their unlocked index. (Will be used internally inside AirbrushPlayer)
    fun getBlocks(type: PaletteType, count: Int): List<String> {
        val palette = get(type)
        val startIndex = 0
        val endIndex = startIndex + count

        if (endIndex > palette.size) {
            throw IllegalArgumentException("Invalid endIndex")
        }

        return palette.subList(startIndex, endIndex)
    }
}