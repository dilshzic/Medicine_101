package com.algorithmx.medicine101.brain.models

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
    TEXT_TO_TEXT,
    VISION,
    MULTILINGUAL
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
