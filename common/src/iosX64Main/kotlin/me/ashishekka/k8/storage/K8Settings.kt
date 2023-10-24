package me.ashishekka.k8.storage

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow

actual class K8Settings {

    val userSettings = NSUserDefaultsSettings.Factory().create(SETTINGS_NAME).toFlowSettings()

    @NativeCoroutines
    actual fun getBooleanFlowSetting(key: String): Flow<Boolean> {
        return userSettings.getBooleanFlow(key, false)
    }

    @NativeCoroutines
    actual suspend fun setBooleanSetting(key: String, value: Boolean) {
        userSettings.putBoolean(key, value)
    }

    @NativeCoroutines
    actual fun getIntFlowSetting(key: String): Flow<Int> {
        return userSettings.getIntFlow(key, 0)
    }

    @NativeCoroutines
    actual suspend fun setIntSetting(key: String, value: Int) {
        userSettings.putInt(key, value)
    }

    @NativeCoroutines
    actual suspend fun getBooleanSetting(key: String): Boolean {
        return userSettings.getBoolean(key, false)
    }

    @NativeCoroutines
    actual suspend fun getIntSetting(key: String): Int {
        return userSettings.getInt(key, 0)
    }
}
