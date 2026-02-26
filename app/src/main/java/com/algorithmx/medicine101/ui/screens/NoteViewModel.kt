package com.algorithmx.medicine101.ui.screens

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.brain.AiBrainManager
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.ContentBlockEntity
import com.algorithmx.medicine101.data.NoteRepository
import com.algorithmx.medicine101.data.remote.CloudSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val aiBrainManager: AiBrainManager,
    private val cloudRepository: CloudSyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "NoteViewModel"
    private val noteId: String = savedStateHandle["noteId"] ?: throw IllegalArgumentException("Note ID required")
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    private val _blocks = MutableStateFlow<List<ContentBlock>>(emptyList())
    val blocks: StateFlow<List<ContentBlock>> = _blocks.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _selectedTab = MutableStateFlow("General")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
        - Differential Diagnosis likelihood must be one of: "High", "Medium", "Red Flag".
    """.trimIndent()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            try {
                val noteWithBlocks = repository.getNoteWithBlocks(noteId)
                if (noteWithBlocks != null) {
                    _title.value = noteWithBlocks.note.title
                    val uiBlocks = noteWithBlocks.blocks
                        .sortedBy { it.orderIndex }
                        .map { entity ->
                            val parsedBlock = json.decodeFromString<ContentBlock>(entity.content)
                            parsedBlock.copy(tabName = entity.tabName ?: "General")
                        }
                    _blocks.value = uiBlocks
                    val firstTab = uiBlocks.firstOrNull()?.tabName ?: "General"
                    _selectedTab.value = firstTab
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load note: ${e.message}", e)
                _error.value = "Failed to load note contents."
            }
        }
    }

    fun generateAiContent(instructions: String? = null) {
        if (_title.value.isBlank()) {
            _error.value = "Please provide a note title before generating content."
            return
        }
        
        viewModelScope.launch {
            _isAiLoading.value = true
            _error.value = null
            try {
                val sectionPrompt = if (_selectedTab.value != "General") " focus specifically on the section '${_selectedTab.value}'" else ""
                val prompt = """
                    $baseSystemPrompt
                    Topic: "${_title.value}"
                    Task: Generate a comprehensive medical note about this topic$sectionPrompt.
                    Instructions: ${instructions ?: "Provide standard medical details."}
                """.trimIndent()

                val rawResponse = aiBrainManager.askBrain(prompt)
                
                if (rawResponse.startsWith("Error:")) {
                    _error.value = rawResponse
                    return@launch
                }

                val cleanedJson = rawResponse.removeSurrounding("```json", "```").trim()
                
                val aiBlocks = json.decodeFromString<List<ContentBlock>>(cleanedJson)
                if (aiBlocks.isNotEmpty()) {
                    val blocksWithTabs = aiBlocks.map { it.copy(tabName = _selectedTab.value) }
                    _blocks.update { current -> current + blocksWithTabs }
                }
            } catch (e: Exception) {
                Log.e(TAG, "AI Generation failed: ${e.message}", e)
                _error.value = "AI failed to generate content. Please check your API key or connection."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun refineBlockWithAi(index: Int) {
        val block = _blocks.value.getOrNull(index) ?: return
        if (_title.value.isBlank()) return
        
        viewModelScope.launch {
            _isAiLoading.value = true
            _error.value = null
            try {
                val prompt = """
                    $baseSystemPrompt
                    Topic: "${_title.value}"
                    Task: Generate exactly ONE content block of type "${block.type}".
                    Instructions: ${block.aiInstructions ?: "Provide standard medical details."}
                    Return a JSON array containing exactly ONE object.
                """.trimIndent()

                val rawResponse = aiBrainManager.askBrain(prompt)
                
                if (rawResponse.startsWith("Error:")) {
                    _error.value = rawResponse
                    return@launch
                }

                val cleanedJson = rawResponse.removeSurrounding("```json", "```").trim()
                
                val aiBlocks = json.decodeFromString<List<ContentBlock>>(cleanedJson)
                val newBlock = aiBlocks.firstOrNull()
                
                if (newBlock != null) {
                    updateBlock(index, newBlock.copy(
                        tabName = block.tabName, 
                        aiInstructions = block.aiInstructions 
                    ))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Block refinement failed: ${e.message}", e)
                _error.value = "Failed to refine block. AI response might be malformed."
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun toggleEditMode() {
        _isEditing.update { !it }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun selectTab(tabName: String) {
        _selectedTab.value = tabName
    }

    fun addNewTab(newTabName: String) {
        val ghostBlock = ContentBlock(
            type = "text",
            text = "",
            tabName = newTabName
        )
        _blocks.update { it + ghostBlock }
        selectTab(newTabName)
    }

    fun renameTab(oldName: String, newName: String) {
        if (oldName == newName || newName.isBlank()) return
        
        _blocks.update { currentList ->
            currentList.map { block ->
                if (block.tabName == oldName) {
                    block.copy(tabName = newName)
                } else {
                    block
                }
            }
        }
        
        if (_selectedTab.value == oldName) {
            _selectedTab.value = newName
        }
    }

    fun updateBlock(index: Int, newBlock: ContentBlock) {
        _blocks.update { currentList ->
            val mutableList = currentList.toMutableList()
            if (index in mutableList.indices) {
                mutableList[index] = newBlock
            }
            mutableList
        }
    }

    fun deleteBlock(index: Int) {
        _blocks.update { currentList ->
            val mutableList = currentList.toMutableList()
            if (index in mutableList.indices) {
                mutableList.removeAt(index)
            }
            mutableList
        }
    }

    fun moveBlock(fromIndex: Int, toIndex: Int) {
        _blocks.update { currentList ->
            val mutableList = currentList.toMutableList()
            if (fromIndex in mutableList.indices && toIndex in 0..mutableList.size) {
                val item = mutableList.removeAt(fromIndex)
                val adjustedToIndex = if (toIndex > fromIndex) toIndex - 1 else toIndex
                mutableList.add(adjustedToIndex, item)
            }
            mutableList
        }
    }

    fun addBlock(block: ContentBlock) {
        _blocks.update { currentList ->
            val blockWithTab = block.copy(tabName = _selectedTab.value)
            currentList + blockWithTab
        }
    }

    fun saveNote() {
        viewModelScope.launch {
            try {
                val noteWithBlocks = repository.getNoteWithBlocks(noteId)
                if (noteWithBlocks != null) {
                    // 1. Update local note
                    val updatedNote = noteWithBlocks.note.copy(
                        title = _title.value,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateNote(updatedNote)

                    // 2. Prepare local block entities
                    val blockEntities = _blocks.value.mapIndexed { index, uiBlock ->
                        ContentBlockEntity(
                            noteId = noteId,
                            type = uiBlock.type,
                            content = json.encodeToString(uiBlock),
                            orderIndex = index,
                            tabName = uiBlock.tabName
                        )
                    }
                    
                    // 3. Sync local blocks
                    repository.syncBlocks(noteId, blockEntities)
                    
                    // 4. Backup to Cloud
                    cloudRepository.backupNoteToCloud(updatedNote, blockEntities)

                    _isEditing.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Save failed: ${e.message}", e)
                _error.value = "Failed to save changes. Please try again."
            }
        }
    }
}
