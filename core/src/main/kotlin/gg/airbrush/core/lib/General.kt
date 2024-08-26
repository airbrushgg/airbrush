package gg.airbrush.core.lib

import java.util.*

fun setInterval(interval: Long, task: () -> Unit): Timer {
    val timer = Timer(true)
    timer.scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
            task()
        }
    }, 0, interval)
    return timer
}