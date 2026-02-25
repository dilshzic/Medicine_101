package com.algorithmx.medicine101.api

import android.util.Log
import com.algorithmx.medicine101.BuildConfig
import com.algorithmx.medicine101.data.ContentBlock
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiAiService @Inject constructor() {
    
    private val model = GenerativeModel(
        modelName = "gemini-flash-latest", // Using the standard model name
        apiKey = BuildConfig.GEMINI_API_KEY, 
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content { 
            text("You are an expert medical educator. You generate highly structured, accurate, and professional medical notes in JSON format. " +
                 "Ensure all clinical information follows current standard guidelines (e.g., NICE, UpToDate). " +
                 "Always return valid JSON following the requested schema precisely.") 
        }
    )

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    private val baseSystemPrompt = """
        Return a JSON array of objects representing medical content blocks.
        
        Supported block types and their REQUIRED fields:
        
        1. "header": { "type": "header", "text": "Title", "level": 1 or 2 }
        2. "list": { "type": "list", "items": [ { "text": "Point 1", "subItems": [ { "text": "Nested point" } ] } ] }
        3. "table": { "type": "table", "tableHeaders": ["Col A", "Col B"], "tableRows": [ ["Cell 1", "Cell 2"] ] }
        4. "callout": { "type": "callout", "text": "Important note", "variant": "info", "warning", or "error" }
        5. "dd_table": { "type": "dd_table", "ddItems": [ { "finding": "Symptom", "diagnoses": ["D1", "D2"], "likelihood": "High"/"Medium"/"Red Flag" } ] }
        6. "accordion": { "type": "accordion", "items": [ { "title": "Expandable Title", "content": [ { "type": "header", "text": "Internal Title", "level": 2 }, { "type": "list", "items": [...] } ] } ] }

        CRITICAL: 
        - Return ONLY the JSON array. No markdown blocks.
        - Ensure all medical content is concise and evidence-based.
        - Differential Diagnosis likelihood must be one of: "High", "Medium", "Red Flag".
        - Accordion "content" is a nested array of ContentBlocks.
    """.trimIndent()

    suspend fun generateNoteContent(topic: String, section: String? = null): List<ContentBlock> = withContext(Dispatchers.IO) {
        val sectionPrompt = if (section != null && section != "General") " focus specifically on the section '$section'" else ""
        val prompt = """
            $baseSystemPrompt
            
            Topic: "$topic"
            Task: Generate a comprehensive medical note about this topic$sectionPrompt.
        """.trimIndent()

        try {
            Log.d("GeminiAiService", "Generating comprehensive content for: $topic")
            val response = model.generateContent(prompt)
            val jsonString = response.text?.trim() ?: return@withContext emptyList<ContentBlock>()
            
            val cleanedJson = jsonString.removeSurrounding("```json", "```").trim()
            return@withContext json.decodeFromString<List<ContentBlock>>(cleanedJson)
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error generating content: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun refineBlock(topic: String, blockType: String, instructions: String?): ContentBlock? = withContext(Dispatchers.IO) {
        val prompt = """
            $baseSystemPrompt
            
            Topic: "$topic"
            Task: Generate exactly ONE content block of type "$blockType".
            Instructions: ${instructions ?: "Provide standard medical details."}
            
            Return a JSON array containing exactly ONE object.
        """.trimIndent()

        try {
            Log.d("GeminiAiService", "Refining single block: $blockType for $topic")
            val response = model.generateContent(prompt)
            val jsonString = response.text?.trim() ?: return@withContext null
            
            val cleanedJson = jsonString.removeSurrounding("```json", "```").trim()
            val blocks = json.decodeFromString<List<ContentBlock>>(cleanedJson)
            blocks.firstOrNull()
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error refining block: ${e.message}", e)
            null
        }
    }
}
