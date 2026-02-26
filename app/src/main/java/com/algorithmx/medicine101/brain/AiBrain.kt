package com.algorithmx.medicine101.brain

import kotlinx.coroutines.flow.Flow

interface AiBrain {
    suspend fun generateText(prompt: String): Result<String>
    fun generateTextStream(prompt: String): Flow<String>
}
