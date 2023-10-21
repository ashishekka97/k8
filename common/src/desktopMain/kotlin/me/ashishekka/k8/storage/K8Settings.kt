package me.ashishekka.k8.storage

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow

actual class K8Settings {

    val preferences = PreferencesSettings.Factory().create(SETTINGS_NAME).toFlowSettings()

    actual fun getBooleanSetting(key: String): Flow<Boolean> {
        return preferences.getBooleanFlow(key, false)
    }

    actual suspend fun setBooleanSetting(key: String, value: Boolean) {
        preferences.putBoolean(key, value)
    }

    actual fun getIntSetting(key: String): Flow<Int> {
        return preferences.getIntFlow(key, 0)
    }

    actual suspend fun setIntSetting(key: String, value: Int) {
        preferences.putInt(key, value)
    }
}
