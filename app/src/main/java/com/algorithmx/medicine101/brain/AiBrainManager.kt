package com.algorithmx.medicine101.brain

import android.util.Log
import com.algorithmx.medicine101.BuildConfig
import com.algorithmx.medicine101.brain.implementations.GeminiBrainImpl
import com.algorithmx.medicine101.brain.implementations.OpenAiCompatibleBrainImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiBrainManager @Inject constructor(
    private val dataStoreManager: BrainDataStoreManager
) {
    private val TAG = "AiBrainManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _activeBrain = MutableStateFlow<AiBrain?>(null)
    val activeBrainState: StateFlow<AiBrain?> = _activeBrain.asStateFlow()

    private var currentSystemInstruction: String = "You are a helpful medical assistant."

    init {
        dataStoreManager.brainConfigFlow.onEach { config ->
            currentSystemInstruction = config.systemInstruction
            switchBrain(config)
        }.launchIn(scope)
    }

    private fun switchBrain(config: StoredBrainConfig) {
        // Automatically determine provider if not explicitly set or if switching models
        val provider = BrainRegistry.getProviderForModel(config.modelName)
        
        // Strictly use keys from BuildConfig as custom keys were removed
        val apiKey = when (provider) {
            BrainProvider.GEMINI -> BuildConfig.GEMINI_API_KEY
            BrainProvider.GROQ -> BuildConfig.GROQ_API_KEY
            BrainProvider.DEEPSEEK -> BuildConfig.DEEPSEEK_API_KEY
            else -> ""
        }

        Log.d(TAG, "Switching Brain: Model=${config.modelName}, Provider=$provider")

        _activeBrain.value = when (provider) {
            BrainProvider.GEMINI -> {
                GeminiBrainImpl(apiKey, config.modelName)
            }
            BrainProvider.GROQ -> {
                // Correct Groq OpenAI-compatible endpoint
                OpenAiCompatibleBrainImpl(
                    apiKey = apiKey,
                    modelName = config.modelName,
                    baseUrl = "https://api.groq.com/openai/v1/chat/completions"
                )
            }
            BrainProvider.DEEPSEEK -> {
                // Official DeepSeek API endpoint (often without /v1/ for chat completions)
                OpenAiCompatibleBrainImpl(
                    apiKey = apiKey,
                    modelName = config.modelName,
                    baseUrl = "https://api.deepseek.com/chat/completions"
                )
            }
            BrainProvider.LOCAL_GEMMA -> {
                null
            }
        }
    }

    suspend fun askBrain(prompt: String): String {
        val brain = _activeBrain.value ?: return "Error: AI engine not initialized. Please check your settings."

        val fullPrompt = "$currentSystemInstruction\n\nUser Request: $prompt"
        val result = brain.generateText(fullPrompt)
        
        return if (result.isSuccess) {
            val generatedText = result.getOrThrow()
            val estimatedTokens = ((fullPrompt.length + generatedText.length) / 4.0).toInt()
            
            dataStoreManager.incrementUsageStats(estimatedTokens)
            
            generatedText
        } else {
            val error = result.exceptionOrNull()?.message ?: "Unknown error"
            Log.e(TAG, "Brain execution failed: $error")
            "Error: $error"
        }
    }

    fun streamFromBrain(prompt: String): Flow<String> {
        val brain = _activeBrain.value ?: return emptyFlow()
        val fullPrompt = "$currentSystemInstruction\n\nUser Request: $prompt"
        return brain.generateTextStream(fullPrompt)
    }
}
