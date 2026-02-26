package com.algorithmx.medicine101.brain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "brain_settings")

@Singleton
class BrainDataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val PROVIDER = stringPreferencesKey("provider")
        val MODEL = stringPreferencesKey("model")
        val CATEGORY = stringPreferencesKey("category")
        val SYSTEM_INSTRUCTION = stringPreferencesKey("system_instruction")
        val TOTAL_REQUESTS = intPreferencesKey("total_requests")
        val TOTAL_TOKENS = intPreferencesKey("total_tokens")
    }

    val brainConfigFlow: Flow<StoredBrainConfig> = context.dataStore.data.map { preferences ->
        val providerName = preferences[PreferencesKeys.PROVIDER] ?: BrainProvider.GEMINI.name
        val categoryName = preferences[PreferencesKeys.CATEGORY] ?: BrainCategory.TEXT_TO_TEXT.name
        
        StoredBrainConfig(
            provider = try { BrainProvider.valueOf(providerName) } catch (e: Exception) { BrainProvider.GEMINI },
            modelName = preferences[PreferencesKeys.MODEL] ?: "gemini-1.5-flash",
            systemInstruction = preferences[PreferencesKeys.SYSTEM_INSTRUCTION] ?: "You are a helpful medical assistant.",
            totalRequests = preferences[PreferencesKeys.TOTAL_REQUESTS] ?: 0,
            totalTokens = preferences[PreferencesKeys.TOTAL_TOKENS] ?: 0,
            category = try { BrainCategory.valueOf(categoryName) } catch (e: Exception) { BrainCategory.TEXT_TO_TEXT }
        )
    }

    suspend fun saveEngineConfiguration(
        provider: BrainProvider,
        modelName: String,
        category: BrainCategory,
        systemInstruction: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROVIDER] = provider.name
            preferences[PreferencesKeys.MODEL] = modelName
            preferences[PreferencesKeys.CATEGORY] = category.name
            preferences[PreferencesKeys.SYSTEM_INSTRUCTION] = systemInstruction
        }
    }

    suspend fun incrementUsageStats(tokens: Int) {
        context.dataStore.edit { preferences ->
            val currentRequests = preferences[PreferencesKeys.TOTAL_REQUESTS] ?: 0
            val currentTokens = preferences[PreferencesKeys.TOTAL_TOKENS] ?: 0
            preferences[PreferencesKeys.TOTAL_REQUESTS] = currentRequests + 1
            preferences[PreferencesKeys.TOTAL_TOKENS] = currentTokens + tokens
        }
    }
}
