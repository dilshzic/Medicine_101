package com.algorithmx.medicine101.data

import android.content.Context
import com.algorithmx.medicine101.utils.JsonLoader
import com.google.gson.Gson
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
        // 1. Check if already seeded to prevent duplicate data on app restart
        if (!repository.isDatabaseEmpty()) return@withContext

        // 2. Define your Hierarchy here
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
        )

        // 3. Process the map
        seedMap.forEach { (categoryName, files) ->
            // A. Create the Folder for this category
            val folderId = UUID.randomUUID().toString()
            val folder = NoteEntity(
                id = folderId,
                title = categoryName,
                category = "System",
                isFolder = true,
                parentId = null, // Root level folder
                isSystemNote = true
            )
            repository.insertNote(folder)

            // B. Process and map the JSON files inside this Folder
            files.forEach { fileName ->
                try {
                    // 1. Load the raw, nested blocks from JSON
                    val rawJsonBlocks = JsonLoader.loadChapter(context, fileName)

                    // 2. *** FLATTEN THE DATA *** (Extracts tabs into a flat list)
                    val processedBlocks = flattenJsonBlocks(rawJsonBlocks)

                    // 3. Extract title (from the raw blocks, just in case the header was first)
                    val title = rawJsonBlocks.find { it.type == "header" }?.text
                        ?: fileName.removeSuffix(".json").replace("_", " ").capitalize()

                    val noteId = UUID.randomUUID().toString()

                    // 4. Create and insert the parent NoteEntity
                    val note = NoteEntity(
                        id = noteId,
                        title = title,
                        category = categoryName,
                        isFolder = false,
                        parentId = folderId, // Assign to the folder we just created
                        isSystemNote = true
                    )
                    repository.insertNote(note)

                    // 5. Map the FLATTENED blocks to the Database Entities
                    val gson = Gson()
                    val blockEntities = processedBlocks.mapIndexed { index, block ->
                        ContentBlockEntity(
                            noteId = noteId,
                            type = block.type,
                            content = gson.toJson(block), // Serialize the individual flattened block
                            orderIndex = index,
                            tabName = block.tabName // This now correctly holds "General" or the Tab title!
                        )
                    }

                    // 6. Insert all mapped blocks into Room
                    repository.insertBlocks(blockEntities)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Helper function to take nested tab JSON and turn it into a flat list
     * where every block knows which tab it belongs to.
     */
    private fun flattenJsonBlocks(rawBlocks: List<ContentBlock>): List<ContentBlock> {
        val flatList = mutableListOf<ContentBlock>()

        for (block in rawBlocks) {
            // Check if this block is actually a container for tabs
            if (block.tabs != null && block.tabs.isNotEmpty()) {
                // Loop through each tab (e.g., "Clinical Features", "Management")
                for (tab in block.tabs) {
                    // Loop through the content inside that specific tab
                    for (innerBlock in tab.content) {
                        // Extract the inner block and tag it with the tab's title
                        flatList.add(innerBlock.copy(tabName = tab.title))
                    }
                }
            } else {
                // It is a normal block. Just tag it with "General"
                flatList.add(block.copy(tabName = "General"))
            }
        }

        return flatList
    }

    // Simple extension to capitalize words nicely
    private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}