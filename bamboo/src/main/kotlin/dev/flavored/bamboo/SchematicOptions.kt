package dev.flavored.bamboo

import net.minestom.server.instance.block.Block

data class SchematicOptions(
    /**
     * Whether to ignore air blocks when pasting the schematic. Defaults to false.
     */
    val ignoreAir: Boolean = false,
    /**
     * When an incompatible block is encountered, it will be replaced with [replacementBlock].
     * Defaults to [Block.AIR].
     */
    val replacementBlock: Block = Block.AIR,
)
