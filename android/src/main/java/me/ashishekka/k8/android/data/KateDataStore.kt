package me.ashishekka.k8.android.data

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val DATASTORE_NAME = "settings"
const val KEY_SOUND = "sound"
const val KEY_HAPTICS = "haptics"
const val KEY_THEME = "theme"
const val KEY_SPEED = "speed"
const val KEY_VERSION = "version"

interface KateDataStore {

    fun getBooleanPreference(key: String): Flow<Boolean>

    suspend fun setBooleanPreference(key: String, value: Boolean)

    fun getIntPreference(key: String): Flow<Int>

    suspend fun setIntPreference(key: String, value: Int)
}

class KateDataStoreImpl(
    private val applicationContext: Application
) : KateDataStore {

    override fun getBooleanPreference(key: String): Flow<Boolean> {
        return applicationContext.datastore.data.map { it[booleanPreferencesKey(key)] ?: false }
    }

    override suspend fun setBooleanPreference(key: String, value: Boolean) {
        applicationContext.datastore.edit { settings ->
            settings[booleanPreferencesKey(key)] = value
        }
    }

    override fun getIntPreference(key: String): Flow<Int> {
        return applicationContext.datastore.data.map { it[intPreferencesKey(key)] ?: 0 }
    }

    override suspend fun setIntPreference(key: String, value: Int) {
        applicationContext.datastore.edit { settings ->
            settings[intPreferencesKey(key)] = value
        }
    }

    companion object {
        private val Context.datastore: DataStore<Preferences> by preferencesDataStore(DATASTORE_NAME)
    }
}