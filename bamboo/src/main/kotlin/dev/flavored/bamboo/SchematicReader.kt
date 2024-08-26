package dev.flavored.bamboo

import net.kyori.adventure.nbt.CompoundBinaryTag
import net.minestom.server.instance.block.Block
import net.minestom.server.utils.nbt.BinaryTagReader
import java.io.DataInputStream
import java.io.InputStream

internal class SchematicReader(stream: InputStream, private val options: SchematicOptions) {
    private val reader = BinaryTagReader(DataInputStream(stream))

    private var width: Short = 0
    private var height: Short = 0
    private var length: Short = 0
    private var weOffsetX: Int = 0
    private var weOffsetY: Int = 0
    private var weOffsetZ: Int = 0

    fun read(): Schematic {
        val root = reader.readNameless() as CompoundBinaryTag

        width = root.getShort("Width")
        height = root.getShort("Height")
        length = root.getShort("Length")

        readOffsets(root)
        val palette = readPalette(root)
        val blockData = readBlockData(root, palette)

        return Schematic(width, height, length, weOffsetX, weOffsetY, weOffsetZ, blockData)
    }

    private fun readOffsets(root: CompoundBinaryTag) {
        val metadata = root.getCompound("Metadata") ?: return

        metadata.getInt("WEOffsetX").let { weOffsetX = it }
        metadata.getInt("WEOffsetY").let { weOffsetY = it }
        metadata.getInt("WEOffsetZ").let { weOffsetZ = it }
    }

    private fun readPalette(root: CompoundBinaryTag): Map<Int, Block> {
        val paletteMax = root.getInt("PaletteMax")
        val palette = root.getCompound("Palette")

        if (paletteMax != palette.size())
            throw NBTException("Invalid schematic: Palette does not match expected size")

        return palette.associate { (namespaceID, _) ->
            val block = Block.fromNamespaceId(namespaceID) ?: options.replacementBlock
            palette.getInt(namespaceID)!! to block
        }
    }

    private fun readBlockData(root: CompoundBinaryTag, palette: Map<Int, Block>): List<Block> {
        // If we find a 'BlockData' tag, we probably have a Sponge schematic. Otherwise, try to read an MCEdit schematic.
        if (root.get("BlockData") != null) {
            val blockData = root.getByteArray("BlockData")

            var i = 0
            val blocks = mutableListOf<Block>()
            while (i < blockData.size) {
                val (paletteIndex, size) = readVarInt(blockData, i)

                blocks += palette.getOrDefault(paletteIndex, options.replacementBlock)
                i += size
            }
            return blocks
        } else {
//            val blocks = root.getByteArray("Blocks") ?: throw NBTException("Invalid schematic: missing 'Blocks' tag")
//            val data = root.getByteArray("Data") ?: throw NBTException("Invalid schematic: missing 'Data' tag")
            throw NotImplementedError("The MCEdit format is not supported yet!")
        }
    }

    // https://en.wikipedia.org/wiki/LEB128#Decode_unsigned_integer
    private fun readVarInt(data: ByteArray, offset: Int): Pair<Int, Int> {
        var value = 0
        var size = 0
        while (true) {
            val b = data[offset + size].toInt()
            value = value or (b and 0x7f shl size++ * 7)
            if (b and 0x80 == 0)
                break
        }
        return value to size
    }
}