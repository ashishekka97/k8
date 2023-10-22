package me.ashishekka.k8.configs

enum class EmulatorSpeed(val speedFactor: Float) {
    HALF(0.5f),
    FULL(1.0f),
    ONE_AND_HALF(1.5f),
    DOUBLE(2.0f);

    companion object {
        val map = values().associateBy { it.speedFactor }
        fun getAllSpeeds() = values().map { "${it.speedFactor}X" }
        fun getSpeedFromIndex(index: Int) = try {
            values()[index]
        } catch (ex: Throwable) {
            FULL
        }

        fun getSpeedFromKey(key: Float) = map[key] ?: FULL
    }
}
