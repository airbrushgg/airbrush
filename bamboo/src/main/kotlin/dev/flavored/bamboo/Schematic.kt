package dev.flavored.bamboo

import net.minestom.server.coordinate.Point
import net.minestom.server.instance.Instance
import net.minestom.server.instance.batch.AbsoluteBlockBatch
import net.minestom.server.instance.block.Block

data class Schematic internal constructor(
    val width: Short,
    val height: Short,
    val length: Short,
    val weOffsetX: Int,
    val weOffsetY: Int,
    val weOffsetZ: Int,
    val blocks: List<Block>,
) {
    fun paste(instance: Instance, position: Point) {
        val batch = AbsoluteBlockBatch()

        blocks.forEachIndexed { index, block ->
            val y = index / (width * length)
            val z = index % (width * length) / width
            val x = index % (width * length) % width

            val absolutePos = position.add(x.toDouble() + weOffsetX, y.toDouble() + weOffsetY, z.toDouble() + weOffsetZ)
            instance.loadOptionalChunk(absolutePos).thenRun { batch.setBlock(absolutePos, block) }
        }
        batch.apply(instance, null)
    }
}