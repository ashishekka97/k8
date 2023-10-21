package me.ashishekka.k8.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.startup.Initializer
import me.ashishekka.k8.storage.K8DataStore

class SettingsInitializer : Initializer<DataStore<Preferences>> {

    override fun create(context: Context): DataStore<Preferences> {
        K8DataStore.setup(context.applicationContext)
        return K8DataStore.get()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
