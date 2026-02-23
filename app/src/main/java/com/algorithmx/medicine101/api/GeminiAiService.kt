package com.algorithmx.medicine101.api

import com.algorithmx.medicine101.data.ContentBlock
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiAiService @Inject constructor() {
    // Note: To use this, get an API Key from https://aistudio.google.com/
    private val apiKey = "YOUR_GEMINI_API_KEY" 
    
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content { 
            text("You are an expert medical educator. You generate structured medical notes in JSON format. " +
                 "Ensure all medical information is accurate, concise, and follows standard clinical guidelines.") 
        }
    )

    private val gson = Gson()

    suspend fun generateNoteContent(topic: String, section: String? = null): List<ContentBlock> = withContext(Dispatchers.IO) {
        val sectionPrompt = if (section != null && section != "General") " specifically for the section '$section'" else ""
        val prompt = """
            Generate a structured medical note about "$topic"$sectionPrompt.
            Return a JSON array of objects representing medical content blocks.
            
            Supported block types:
            1. "header": { "type": "header", "text": "Title", "level": 1 (big) or 2 (small) }
            2. "list": { "type": "list", "items": [ { "text": "Point 1" }, { "text": "Point 2" } ] }
            3. "table": { "type": "table", "tableHeaders": ["Col 1", "Col 2"], "tableRows": [["Val 1", "Val 2"]] }
            4. "callout": { "type": "callout", "text": "Crucial warning or pearl" }
            5. "dd": { "type": "dd", "ddItems": [ { "finding": "Symptom", "diagnoses": ["D1", "D2"] } ] }
            6. "accordion": { "type": "accordion", "text": "Summary Title", "items": [ { "title": "Detail Title", "text": "Detail description" } ] }

            Return ONLY the JSON array. Do not include markdown formatting.
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val jsonString = response.text?.trim() ?: return@withContext emptyList<ContentBlock>()
            
            // The model is configured for JSON response, but we still handle potential wrapping
            val cleanedJson = jsonString.removeSurrounding("```json", "```").trim()
            
            val listType = object : TypeToken<List<ContentBlock>>() {}.type
            return@withContext gson.fromJson(cleanedJson, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}