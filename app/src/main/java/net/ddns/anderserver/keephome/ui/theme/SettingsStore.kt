package net.ddns.anderserver.keephome.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsStore(private val context: Context) {
    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        val SYNC_NOTIFICATIONS = booleanPreferencesKey("sync_notifications")

        val AP_MODE = booleanPreferencesKey("ap_mode")
    }

    val getSyncNotifications: Flow<Boolean> = context.dataStore.data
        .map {
            it[SYNC_NOTIFICATIONS] ?: false
        }

    suspend fun setSyncNotifications(state: Boolean) {
        context.dataStore.edit {
            it[SYNC_NOTIFICATIONS] = state
        }
    }

    val getAPMode: Flow<Boolean> = context.dataStore.data
        .map {
            it[AP_MODE] ?: true
        }

    suspend fun setAPMode(state: Boolean) {
        context.dataStore.edit {
            it[AP_MODE] = state
        }
    }
}