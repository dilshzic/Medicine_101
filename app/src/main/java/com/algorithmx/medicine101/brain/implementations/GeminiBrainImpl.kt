package com.algorithmx.medicine101.brain.implementations

import com.algorithmx.medicine101.brain.AiBrain
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeminiBrainImpl(
    apiKey: String,
    modelName: String
) : AiBrain {

    private val generativeModel = GenerativeModel(
        modelName = modelName,
        apiKey = apiKey
    )

    override suspend fun generateText(prompt: String): Result<String> {
        return try {
            val response = generativeModel.generateContent(prompt)
            Result.success(response.text ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun generateTextStream(prompt: String): Flow<String> {
        return generativeModel.generateContentStream(prompt).map { it.text ?: "" }
    }
}
