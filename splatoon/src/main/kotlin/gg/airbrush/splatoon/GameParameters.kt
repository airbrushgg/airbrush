package gg.airbrush.splatoon

class GameParameters {
    private val parameters = mutableMapOf<Parameter, Int>()

    fun get(parameter: Parameter): Int {
        return parameters[parameter] ?: 0
    }

    fun set(parameter: Parameter, value: Int) {
        parameters[parameter] = value
    }

    enum class Parameter(val defaultValue: Int) {
        MIN_PLAYERS(4);
    }
}