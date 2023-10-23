package me.ashishekka.k8.storage

import kotlinx.coroutines.flow.Flow

const val SETTINGS_NAME = "settings"
const val KEY_SOUND = "sound"
const val KEY_HAPTICS = "haptics"
const val KEY_THEME = "theme"
const val KEY_SPEED = "speed"
const val KEY_VERSION = "version"

expect class K8Settings {

    suspend fun getBooleanSetting(key: String): Boolean

    fun getBooleanFlowSetting(key: String): Flow<Boolean>

    suspend fun setBooleanSetting(key: String, value: Boolean)

    suspend fun getIntSetting(key: String): Int

    fun getIntFlowSetting(key: String): Flow<Int>

    suspend fun setIntSetting(key: String, value: Int)
}
