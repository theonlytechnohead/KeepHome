package net.ddns.anderserver.keephome

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsStore(private val context: Context) {
    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        val SYNC_NOTIFICATIONS = booleanPreferencesKey("sync_notifications")
        val NOTIFICATION_INTERVAL = intPreferencesKey("notification_interval")
        val intervalMinutes = listOf(1, 5, 10, 15)

        val AP_MODE = booleanPreferencesKey("ap_mode")
        val SSID = stringPreferencesKey("ssid")
        val PASSWORD = stringPreferencesKey("password")

        val IP = stringPreferencesKey("ip")
    }

//    Sync notifications switch

    val getSyncNotifications: Flow<Boolean> = context.dataStore.data
        .map {
            it[SYNC_NOTIFICATIONS] ?: false
        }

    suspend fun setSyncNotifications(state: Boolean) {
        context.dataStore.edit {
            it[SYNC_NOTIFICATIONS] = state
        }
    }

//    Notification interval

    val getNotificationInterval: Flow<Int> = context.dataStore.data
        .map {
            it[NOTIFICATION_INTERVAL] ?: 1
        }

    suspend fun setNotificationInterval(interval: Int) {
        context.dataStore.edit {
            it[NOTIFICATION_INTERVAL] = interval
        }
    }

//    AP mode switch

    val getAPMode: Flow<Boolean> = context.dataStore.data
        .map {
            it[AP_MODE] ?: true
        }

    suspend fun setAPMode(state: Boolean) {
        context.dataStore.edit {
            it[AP_MODE] = state
        }
    }

//    SSID name

    val getSSID: Flow<String> = context.dataStore.data
        .map {
            it[SSID] ?: "KeepHome"
        }

    suspend fun setSSID(ssid: String) {
        context.dataStore.edit {
            it[SSID] = ssid
        }
    }

//    WiFi password

    val getPassword: Flow<String> = context.dataStore.data
        .map {
            it[PASSWORD] ?: "12345678"
        }

    suspend fun setPassword(password: String) {
        context.dataStore.edit {
            it[PASSWORD] = password
        }
    }

//    IP address

    val getIP: Flow<String> = context.dataStore.data
        .map {
            it[IP] ?: "192.168.4.1"
        }

    suspend fun setIP(ip: String) {
        context.dataStore.edit {
            it[IP] = ip
        }
    }
}