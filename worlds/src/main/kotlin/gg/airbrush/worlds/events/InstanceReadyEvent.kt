package gg.airbrush.worlds.events

import net.minestom.server.event.trait.InstanceEvent
import net.minestom.server.instance.Instance

class InstanceReadyEvent(private val instance: Instance) : InstanceEvent {
    override fun getInstance(): Instance = instance
}