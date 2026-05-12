package com.siscontrol.mobile.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sis_control_session")

data class AppSession(
    val username: String?,
    val userId: Long?,
    val role: String?,
    val installationId: Long?,
    val shiftId: Long?,
    val executionId: Long?,
    val checkpointMap: Map<String, Long>
)

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_ROLE = stringPreferencesKey("user_role")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_INSTALLATION_ID = longPreferencesKey("installation_id")
        private val KEY_SHIFT_ID = longPreferencesKey("shift_id")
        private val KEY_EXECUTION_ID = longPreferencesKey("execution_id")
        private val KEY_CHECKPOINT_MAP = stringPreferencesKey("checkpoint_map")
    }

    suspend fun saveSession(username: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
            preferences[KEY_ROLE] = normalizeRole(role)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_INSTALLATION_ID)
            preferences.remove(KEY_SHIFT_ID)
            preferences.remove(KEY_EXECUTION_ID)
            preferences.remove(KEY_CHECKPOINT_MAP)
        }
    }

    suspend fun saveGuardContext(userId: Long, installationId: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_INSTALLATION_ID] = installationId
        }
    }

    suspend fun saveShiftId(shiftId: Long) {
        context.dataStore.edit { it[KEY_SHIFT_ID] = shiftId }
    }

    suspend fun clearShiftId() {
        context.dataStore.edit { it.remove(KEY_SHIFT_ID) }
    }

    suspend fun saveExecutionId(executionId: Long) {
        context.dataStore.edit { it[KEY_EXECUTION_ID] = executionId }
    }

    suspend fun clearExecutionId() {
        context.dataStore.edit { it.remove(KEY_EXECUTION_ID) }
    }

    suspend fun saveInstallationId(installationId: Long) {
        context.dataStore.edit { it[KEY_INSTALLATION_ID] = installationId }
    }

    suspend fun saveCheckpointMap(map: Map<String, Long>) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CHECKPOINT_MAP] = map.entries.joinToString("|") { (code, id) ->
                "${code.uppercase()}:$id"
            }
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { null }
    val roleFlow: Flow<String?> = context.dataStore.data.map { it[KEY_ROLE] }
    val sessionFlow: Flow<AppSession> = context.dataStore.data.map { preferences ->
        AppSession(
            username = preferences[KEY_USERNAME],
            userId = preferences[KEY_USER_ID],
            role = preferences[KEY_ROLE],
            installationId = preferences[KEY_INSTALLATION_ID],
            shiftId = preferences[KEY_SHIFT_ID],
            executionId = preferences[KEY_EXECUTION_ID],
            checkpointMap = parseCheckpointMap(preferences[KEY_CHECKPOINT_MAP])
        )
    }

    fun getTokenSync(): String? = null

    fun getSessionSync(): AppSession = runBlocking { sessionFlow.first() }

    suspend fun clearSession() {
        context.dataStore.edit { preferences -> preferences.clear() }
    }

    private fun normalizeRole(role: String): String = when (role.uppercase()) {
        "GUARDIA" -> "GUARD"
        else -> role.uppercase()
    }

    private fun parseCheckpointMap(raw: String?): Map<String, Long> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split("|")
            .mapNotNull { entry ->
                val parts = entry.split(":", limit = 2)
                val id = parts.getOrNull(1)?.toLongOrNull()
                val code = parts.getOrNull(0)?.uppercase()
                if (!code.isNullOrBlank() && id != null) code to id else null
            }
            .toMap()
    }
}
