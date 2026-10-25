package com.jeremy.kampai.ui.theme.warmup

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.warmupDataStore by preferencesDataStore(name = "warmup_preferences")

/**
 * Maneja la persistencia de preferencias propias del minijuego WarmUp,
 * como si el usuario ya vio el tutorial de tooltips.
 *
 * Sigue el mismo patrón que ThemeManager para mantener consistencia
 * en cómo Kampai usa DataStore.
 */
@Singleton
class WarmupPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val hasSeenTutorialKey = booleanPreferencesKey("has_seen_warmup_tutorial")

    val hasSeenTutorial: Flow<Boolean> = context.warmupDataStore.data.map { preferences ->
        preferences[hasSeenTutorialKey] ?: false
    }

    suspend fun setHasSeenTutorial(seen: Boolean) {
        context.warmupDataStore.edit { preferences ->
            preferences[hasSeenTutorialKey] = seen
        }
    }
}