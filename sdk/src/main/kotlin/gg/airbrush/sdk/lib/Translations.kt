package gg.airbrush.sdk.lib

import com.moandjiezana.toml.Toml
import gg.airbrush.sdk.Database
import java.nio.file.Files

object Translations {
	private lateinit var translations: Toml

	init {
		loadTranslations()
	}

	private fun loadTranslations() {
		val translationsPath = ConfigUtils.loadResource(
			clazz = Database::class.java,
			fileName = "translations.toml",
			folder = "sdk"
		)

		val data = String(Files.readAllBytes(translationsPath))
		translations = Toml().read(data)
	}

	fun translate(key: String, vararg args: Any): String {
		val translation = translations.getString("en.$key") ?: return key
		val formatted = String.format(translation, *args)
		return formatted.trimIndent()
	}

	fun reload() {
		loadTranslations()
	}
}
