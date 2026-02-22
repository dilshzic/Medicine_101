package com.algorithmx.medicine101.data

import android.content.Context
import com.algorithmx.medicine101.utils.JsonLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: NoteRepository
) {
    suspend fun seedDatabase() = withContext(Dispatchers.IO) {
        // 1. Check if already seeded
        if (!repository.isDatabaseEmpty()) return@withContext

        // 2. Define your Hierarchy here
        // Structure: "Folder Name" -> List of "json_filename.json"
        val seedMap = mapOf(
            "Examinations" to listOf(
                "abdomen.json", "cardio.json", "resp.json",
                "neuro_cranial.json", "neuro_upper.json", "neuro_lower.json",
                "gait.json", "general_exam.json"
            ),
            "Long Cases" to listOf(
                "long_diabetes.json", "long_stroke.json", "long_jaundice.json",
                "long_anaemia.json", "long_pneumonia.json", "long_oedema.json",
                "long_back_pain.json", "long_weight_loss.json"
            ),
            "Symptomatology" to listOf(
                "long_chest_pain.json", "long_cough.json", "long_dyspnoea.json",
                "long_bleeding.json"
            )
            // Add other groupings as needed
        )

        // 3. Process the map
        seedMap.forEach { (categoryName, files) ->
            // A. Create the Folder
            val folderId = UUID.randomUUID().toString()
            val folder = NoteEntity(
                id = folderId,
                title = categoryName,
                category = "System",
                isFolder = true,
                parentId = null, // Root level
                isSystemNote = true
            )
            repository.insertNote(folder)

            // B. Create Notes inside this Folder
            files.forEach { fileName ->
                try {
                    // Read JSON raw string
                    val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }

                    // Parse title using your existing utility (or filename fallback)
                    // We parse purely to extract the title, but store the raw string
                    val title = extractTitleFromJson(context, fileName)

                    val note = NoteEntity(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        category = categoryName,
                        //contentJson = jsonString, // Store the raw JSON content directly
                        isFolder = false,
                        parentId = folderId, // Put inside the folder
                        isSystemNote = true
                    )
                    repository.insertNote(note)

                } catch (e: Exception) {
                    e.printStackTrace()
                    // Continue to next file if one fails
                }
            }
        }
    }

    // Helper to extract a pretty title from the JSON content
    // This assumes your JSONs have a "header" block or similar.
    // If not, it falls back to the filename.
    private fun extractTitleFromJson(context: Context, fileName: String): String {
        return try {
            // Change loadJsonFromAssets to loadChapter (which is what you have in JsonLoader)
            val blocks = JsonLoader.loadChapter(context, fileName)

            // Find the first header block
            val headerBlock = blocks.find { it.type == "header" }

            // Return header text or a formatted version of the filename
            headerBlock?.text ?: fileName.removeSuffix(".json")
                .replace("_", " ")
                .replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            fileName.removeSuffix(".json")
        }
    }

    // Simple extension to capitalize words
    private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}