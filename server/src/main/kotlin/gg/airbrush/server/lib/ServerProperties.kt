package gg.airbrush.server.lib

import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import java.io.File
import kotlin.reflect.KProperty

private val file = File("server.properties")
private lateinit var properties: Map<String, String>

object ServerProperties {
    init {
        if (file.exists()) {
            properties = mapOf(*file.readText().split("\n").filter { it.isNotEmpty() }.map {
                val (key, value) = it.split("=")
                key.lowercase() to value
            }.toTypedArray())
        } else properties = emptyMap()
    }

    class Field<T>(private val key: String, private val type: Class<T>, private val default: T) {
        operator fun getValue(thisRef: ServerProperties, property: KProperty<*>): T {
            val raw = properties[key.lowercase()]

            if (raw.isNullOrEmpty())
                return default

            @Suppress("UNCHECKED_CAST")
            return when (type) {
                String::class.java -> raw as T
                Int::class.java -> raw.toInt() as T
                Boolean::class.java -> raw.toBoolean() as T
                CompoundBinaryTag::class.java -> TagStringIO.get().asCompound(raw) as T
                else -> throw IllegalArgumentException("Unknown type ${type.name}")
            }
        }
    }

    val maxPlayers by Field("max-players", Int::class.java, 10)
    val motd by Field("motd", String::class.java, "A Minestom Server")
}