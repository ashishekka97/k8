package me.ashishekka.k8.storage

import com.russhwolf.settings.StorageSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 *  Note:- Flow APIs in this K8 implementation will not work for same page.
 *  Mote info: https://developer.mozilla.org/en-US/docs/Web/API/Window/storage_event
 */
actual class K8Settings {

    val storageSettings = StorageSettings()

    actual fun getBooleanFlowSetting(key: String): Flow<Boolean> {
        return emptyFlow()
    }

    actual suspend fun setBooleanSetting(key: String, value: Boolean) {
        storageSettings.putBoolean(key, value)
    }

    actual fun getIntFlowSetting(key: String): Flow<Int> {
        return emptyFlow()
    }

    actual suspend fun setIntSetting(key: String, value: Int) {
        storageSettings.putInt(key, value)
    }

    actual suspend fun getBooleanSetting(key: String): Boolean {
        return storageSettings.getBoolean(key, false)
    }

    actual suspend fun getIntSetting(key: String): Int {
        return storageSettings.getInt(key, 0)
    }

    fun getBoolean(key: String): Boolean {
        return storageSettings.getBoolean(key, false)
    }

    fun getInt(key: String): Int {
        return storageSettings.getInt(key, 0)
    }
}
