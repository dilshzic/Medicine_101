package com.algorithmx.medicine101.brain

import kotlinx.serialization.Serializable

enum class BrainProvider {
    GEMINI,
    GROQ,
    DEEPSEEK,
    LOCAL_GEMMA
}

enum class BrainCategory {
    REASONING,
    FUNCTION_CALLING,
    TEXT_TO_SPEECH,
    SPEECH_TO_TEXT,
    TEXT_TO_TEXT,
    VISION,
    MULTILINGUAL,
    SAFETY
}

@Serializable
data class BrainConfig(
    val provider: BrainProvider,
    val modelName: String,
    val category: BrainCategory = BrainCategory.TEXT_TO_TEXT
)

data class StoredBrainConfig(
    val provider: BrainProvider,
    val modelName: String,
    val systemInstruction: String,
    val totalRequests: Int,
    val totalTokens: Int,
    val category: BrainCategory = BrainCategory.TEXT_TO_TEXT
)

object BrainRegistry {
    // Models mapped to their Groq/Gemini/DeepSeek codes
    val categoryMap = mapOf(
        BrainCategory.REASONING to listOf(
            "gpt-oss-120b", 
            "gpt-oss-20b", 
            "qwen-3-32b-instruct",
            "gemini-3.1-pro",
            "gemini-3-pro",
            "gemini-2.5-pro",
            "gemma-3-27b-it"
        ),
        BrainCategory.FUNCTION_CALLING to listOf(
            "gpt-oss-120b", 
            "gpt-oss-20b", 
            "llama-4-scout-17b-16e-instruct", 
            "qwen-3-32b-instruct", 
            "kimi-k2-instruct",
            "gemini-3.1-pro",
            "gemini-3-pro",
            "gemini-3-flash",
            "gemini-2.5-pro",
            "gemini-2.5-flash"
        ),
        BrainCategory.TEXT_TO_SPEECH to listOf(
            "orpheus-v1-english", 
            "orpheus-v1-arabic-saudi"
        ),
        BrainCategory.SPEECH_TO_TEXT to listOf(
            "whisper-large-v3", 
            "whisper-large-v3-turbo"
        ),
        BrainCategory.TEXT_TO_TEXT to listOf(
            "gpt-oss-120b", 
            "gpt-oss-20b", 
            "kimi-k2-instruct", 
            "llama-4-scout-17b-16e-instruct", 
            "llama-3.3-70b-versatile",
            "gemini-3.1-pro",
            "gemini-3-pro",
            "gemini-3-flash",
            "gemini-2.5-pro",
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",
            "gemma-3-27b-it",
            "gemma-3-12b-it",
            "gemma-3-4b-it"
        ),
        BrainCategory.VISION to listOf(
            "llama-4-scout-17b-16e-instruct",
            "gemini-3.1-pro",
            "gemini-3-pro",
            "gemini-3-flash",
            "gemini-2.5-pro",
            "gemini-2.5-flash"
        ),
        BrainCategory.MULTILINGUAL to listOf(
            "gpt-oss-120b", 
            "gpt-oss-20b", 
            "kimi-k2-instruct", 
            "llama-4-scout-17b-16e-instruct", 
            "llama-3.3-70b-versatile", 
            "whisper-large-v3",
            "gemini-3.1-pro",
            "gemini-3-pro",
            "gemini-3-flash",
            "gemini-2.5-pro",
            "gemini-2.5-flash"
        ),
        BrainCategory.SAFETY to listOf(
            "safety-gpt-oss-20b"
        )
    )

    fun getProviderForModel(modelName: String): BrainProvider {
        return when {
            modelName.startsWith("gemini") -> BrainProvider.GEMINI
            modelName.startsWith("gemma-3") -> BrainProvider.GEMINI
            modelName.startsWith("deepseek") -> BrainProvider.DEEPSEEK
            modelName.startsWith("gemma") -> BrainProvider.LOCAL_GEMMA
            else -> BrainProvider.GROQ
        }
    }
}
