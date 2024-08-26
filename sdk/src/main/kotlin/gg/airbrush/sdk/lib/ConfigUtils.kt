package gg.airbrush.sdk.lib

import gg.airbrush.server.plugins.PluginInfo
import java.io.File
import java.io.InputStream
import java.nio.file.Path

object ConfigUtils {
    fun loadResource(clazz: Class<*>, fileName: String, pluginInfo: PluginInfo): Path {
        val directory = File("plugins/${pluginInfo.id}")
	    return loadResource(clazz, fileName, directory)
    }

	fun loadResource(clazz: Class<*>, fileName: String, folder: String): Path {
		val directory = File("plugins/$folder")
		return loadResource(clazz, fileName, directory)
	}

	private fun loadResource(clazz: Class<*>, fileName: String, directory: File): Path {
		if (!directory.exists()) directory.mkdirs()

		val outputFile = File(directory, fileName)

		if(outputFile.exists()) return outputFile.toPath()

		val resourceStream: InputStream? = clazz.getResourceAsStream("/$fileName")

		resourceStream?.use { input ->
			outputFile.outputStream().use { output ->
				input.copyTo(output)
			}
		}

		return outputFile.toPath()
	}
}