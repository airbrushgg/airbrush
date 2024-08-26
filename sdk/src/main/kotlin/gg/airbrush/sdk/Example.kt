package gg.airbrush.sdk

import gg.airbrush.sdk.lib.Translations

fun logText(): String {
	return Translations.translate("core.welcome", "nerd")
}

fun main() {
	println(logText())
}

