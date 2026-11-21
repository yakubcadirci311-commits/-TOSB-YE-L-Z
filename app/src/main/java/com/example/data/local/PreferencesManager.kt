package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tertemiz_prefs")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val HAS_INITIALIZED = booleanPreferencesKey("has_initialized")
        val CHECKED_CHECKLIST_IDS = stringSetPreferencesKey("checked_checklist_ids")
        val SELECTED_TIP_INDEX = intPreferencesKey("selected_tip_index")
        val UNLOCKED_BADGES = stringSetPreferencesKey("unlocked_badges")
    }

    val checkedChecklistIds: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.CHECKED_CHECKLIST_IDS] ?: emptySet()
    }

    val selectedTipIndex: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_TIP_INDEX] ?: 0
    }

    // "rozetId:timestamp" formatındaki kayıtları Map'e dönüştürür
    val unlockedBadges: Flow<Map<String, Long>> = context.dataStore.data.map { prefs ->
        val rawSet = prefs[Keys.UNLOCKED_BADGES] ?: emptySet()
        rawSet.mapNotNull { entry ->
            val parts = entry.split(":", limit = 2)
            val id = parts.getOrNull(0) ?: return@mapNotNull null
            val ts = parts.getOrNull(1)?.toLongOrNull() ?: 0L
            id to ts
        }.toMap()
    }

    suspend fun hasInitialized(): Boolean {
        return context.dataStore.data.first()[Keys.HAS_INITIALIZED] ?: false
    }

    suspend fun markInitialized() {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_INITIALIZED] = true
        }
    }

    suspend fun setCheckedChecklistIds(ids: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CHECKED_CHECKLIST_IDS] = ids
        }
    }

    suspend fun toggleChecklistId(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CHECKED_CHECKLIST_IDS] ?: emptySet()
            prefs[Keys.CHECKED_CHECKLIST_IDS] = if (id in current) current - id else current + id
        }
    }

    suspend fun setSelectedTipIndex(index: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_TIP_INDEX] = index
        }
    }

    suspend fun addUnlockedBadges(newEntries: Map<String, Long>) {
        if (newEntries.isEmpty()) return
        context.dataStore.edit { prefs ->
            val existing = (prefs[Keys.UNLOCKED_BADGES] ?: emptySet()).toMutableSet()
            val existingIds = existing.map { it.substringBefore(":") }.toSet()
            newEntries.forEach { (id, timestamp) ->
                if (id !in existingIds) {
                    existing.add("$id:$timestamp")
                }
            }
            prefs[Keys.UNLOCKED_BADGES] = existing
        }
    }

    suspend fun setUnlockedBadges(entries: Map<String, Long>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UNLOCKED_BADGES] = entries.map { (id, ts) -> "$id:$ts" }.toSet()
        }
    }

    suspend fun resetUserPreferences() {
        context.dataStore.edit { prefs ->
            prefs[Keys.CHECKED_CHECKLIST_IDS] = emptySet()
            prefs[Keys.SELECTED_TIP_INDEX] = 0
            prefs[Keys.UNLOCKED_BADGES] = emptySet()
            // İlk kurulum bayrağı korunur, böylece sıfırlanan uygulama sonraki açılışta boş kalır
            prefs[Keys.HAS_INITIALIZED] = true
        }
    }
}
