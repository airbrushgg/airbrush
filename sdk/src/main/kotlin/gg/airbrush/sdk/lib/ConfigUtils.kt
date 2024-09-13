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