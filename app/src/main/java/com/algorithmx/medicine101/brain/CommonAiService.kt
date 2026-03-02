package com.algorithmx.medicine101.brain

import android.util.Log
import com.algorithmx.medicine101.data.models.AiGeneratedBlock
import com.algorithmx.medicine101.data.models.BlockType
import com.algorithmx.medicine101.data.models.NoteContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonAiService @Inject constructor(
    private val aiBrainManager: AiBrainManager
) {
    private val TAG = "CommonAiService"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Generates a list of content blocks based on the provided topic and note context.
     */
    suspend fun generateBlocksForTopic(
        topic: String,
        context: NoteContext,
        requestedTypes: List<BlockType> = listOf(BlockType.HEADER, BlockType.TEXT, BlockType.LIST)
    ): Result<List<AiGeneratedBlock>> {
        
        val prompt = constructGenerationPrompt(topic, context, requestedTypes)
        
        return try {
            val rawResponse = aiBrainManager.askBrain(prompt)
            
            // Extract JSON from the response (in case the AI adds markdown backticks)
            val jsonString = extractJsonFromResponse(rawResponse)
            
            val blocks = json.decodeFromString<List<AiGeneratedBlock>>(jsonString)
            Result.success(blocks)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate blocks: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun constructGenerationPrompt(
        topic: String,
        context: NoteContext,
        requestedTypes: List<BlockType>
    ): String {
        val typesString = requestedTypes.joinToString(", ") { it.name }
        
        return """
            You are a medical content generator. Your task is to generate highly structured and accurate medical information.
            
            TOPIC: $topic
            
            CONTEXT:
            - Note Title: ${context.noteTitle}
            - Category: ${context.noteCategory}
            ${context.parentNoteTitle?.let { "- Parent Note: $it" } ?: ""}
            ${context.existingContentSummary?.let { "- Existing Content Summary: $it" } ?: ""}
            
            INSTRUCTIONS:
            1. Generate content relevant to the TOPIC while staying consistent with the CONTEXT.
            2. Return ONLY a valid JSON array of objects. 
            3. Each object must have "type" (String) and "content" (String) fields.
            4. The "type" field must be one of: $typesString.
            5. For LIST type, format the "content" as a simple bulleted list with \n separators.
            6. For ACCORDION type, format the "content" as "Title;Detailed content here".
            7. For TABLE type, provide a standard markdown table.
            
            CRITICAL: Do not include any text outside of the JSON array. Do not use markdown backticks unless strictly necessary inside the content.
        """.trimIndent()
    }

    private fun extractJsonFromResponse(response: String): String {
        val startIndex = response.indexOf("[")
        val endIndex = response.lastIndexOf("]")
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1)
        }
        return response
    }
}
