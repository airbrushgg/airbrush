package gg.airbrush.core.lib

import net.minestom.server.item.Material

fun String.prettify(): String {
    return this
        .replace("minecraft:", "")
        .replace(Regex("[-_]"), " ")
        .split(" ")
        .joinToString(" ") {
            ((it.getOrNull(0) ?: "").toString()).uppercase() +
                    it.substring(it.length.coerceAtMost(1)).lowercase()
        }
}

fun String.toMaterial(): Material? {
    return Material.fromNamespaceId("minecraft:$this")
}