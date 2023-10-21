package me.ashishekka.k8.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.russhwolf.settings.datastore.DataStoreSettings
import kotlinx.coroutines.flow.Flow

actual class K8Settings {

    val dataStoreSettings = DataStoreSettings(K8DataStore.get())

    actual fun getBooleanSetting(key: String): Flow<Boolean> {
        return dataStoreSettings.getBooleanFlow(key, false)
    }

    actual suspend fun setBooleanSetting(key: String, value: Boolean) {
        dataStoreSettings.putBoolean(key, value)
    }

    actual fun getIntSetting(key: String): Flow<Int> {
        return dataStoreSettings.getIntFlow(key, 0)
    }

    actual suspend fun setIntSetting(key: String, value: Int) {
        dataStoreSettings.putInt(key, value)
    }
}

object K8DataStore {

    private val Context.datastore: DataStore<Preferences> by preferencesDataStore(SETTINGS_NAME)

    private lateinit var dataStore: DataStore<Preferences>

    fun setup(context: Context) {
        dataStore = context.datastore
    }

    fun get(): DataStore<Preferences> {
        if (::dataStore.isInitialized.not()) {
            throw Exception(
                "Application context isn't initialized"
            )
        }
        return dataStore
    }
}
