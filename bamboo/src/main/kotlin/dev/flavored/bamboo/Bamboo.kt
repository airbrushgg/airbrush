package dev.flavored.bamboo

import java.io.File
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

object Bamboo {
    /**
     * Builds a [Schematic] from an input stream.
     */
    @JvmStatic
    fun fromStream(stream: InputStream, options: SchematicOptions = SchematicOptions()) =
        SchematicReader(stream, options).read()

    /**
     * Builds a [Schematic] from a file.
     */
    @JvmStatic
    fun fromFile(file: File, options: SchematicOptions = SchematicOptions()) =
        fromStream(file.inputStream(), options)

    /**
     * Builds a [Schematic] from a path.
     */
    @JvmStatic
    fun fromPath(path: Path, options: SchematicOptions = SchematicOptions()) =
        fromStream(path.inputStream(), options)
}