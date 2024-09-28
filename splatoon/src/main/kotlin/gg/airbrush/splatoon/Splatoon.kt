package gg.airbrush.splatoon

import gg.airbrush.server.plugins.Plugin
import gg.airbrush.splatoon.event.Form
import gg.airbrush.splatoon.event.GameEntry

class Splatoon : Plugin() {
    override fun setup() {
        registerEvents()
    }

    override fun teardown() {}

    private fun registerEvents() {
        GameEntry()
        Form()
    }
}