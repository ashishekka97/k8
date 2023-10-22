package me.ashishekka.k8.storage

import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow

/**
 *  Note:- Flow APIs in this K8 implementation will not work for same page.
 *  Mote info: https://developer.mozilla.org/en-US/docs/Web/API/Window/storage_event
 */
actual class K8Settings {

    val storageSettings = StorageSettings().asObservableSettings().toFlowSettings()

    actual fun getBooleanSetting(key: String): Flow<Boolean> {
        return storageSettings.getBooleanFlow(key, false)
    }

    actual suspend fun setBooleanSetting(key: String, value: Boolean) {
        storageSettings.putBoolean(key, value)
    }

    actual fun getIntSetting(key: String): Flow<Int> {
        return storageSettings.getIntFlow(key, 0)
    }

    actual suspend fun setIntSetting(key: String, value: Int) {
        storageSettings.putInt(key, value)
    }
}
