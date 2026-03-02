package com.algorithmx.medicine101.brain.registry

import com.algorithmx.medicine101.brain.models.BrainCategory
import com.algorithmx.medicine101.brain.models.BrainProvider

object BrainRegistry {
    // Valid Model IDs for Groq and Gemini as of late 2024/early 2025
    val categoryMap = mapOf(
        BrainCategory.REASONING to listOf(
            // Gemini Flash Family (Fast text generation)
            "gemini-3-flash",
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite",

            // Gemma 3 Family (Google's open-weights hosted on Gemini API)
            "gemma-3-1b-it",
            "gemma-3-2b-it",
            "gemma-3-4b-it",
            "gemma-3-12b-it",
            "gemma-3-27b-it"
        ),
        BrainCategory.FUNCTION_CALLING to listOf(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
        ),
        BrainCategory.TEXT_TO_TEXT to listOf(
            "qwen/qwen3-32b",
            "groq/compound",
            "groq/compound-mini",
            "llama-3.1-8b-instant",
            "llama-3.3-70b-versatile",
            "meta-llama/llama-4-scout-17b-16e-instruct",
            "meta-llama/llama-prompt-guard-2-22m",
            "meta-llama/llama-prompt-guard-2-86m",
            "moonshotai/kimi-k2-instruct-0905",
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b",
            "openai/gpt-oss-safeguard-20b"
        ),
        BrainCategory.VISION to listOf(
            "llama-3.2-11b-vision-preview",
            "llama-3.2-90b-vision-preview",
            "gemini-1.5-pro",
            "gemini-1.5-flash"
        ),
        BrainCategory.MULTILINGUAL to listOf(
            "llama-3.3-70b-versatile",
            "gemini-1.5-pro",
            "gemini-1.5-flash"
        )
    )

    fun getProviderForModel(modelName: String): BrainProvider {
        return when {
            modelName.startsWith("gem") -> BrainProvider.GEMINI
            modelName.startsWith("deepseek") -> BrainProvider.DEEPSEEK
            modelName.startsWith("gemma-local") -> BrainProvider.LOCAL_GEMMA
            else -> BrainProvider.GROQ
        }
    }
}
