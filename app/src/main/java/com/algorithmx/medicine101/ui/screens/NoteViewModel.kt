package com.algorithmx.medicine101.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.api.GeminiAiService
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
    private val geminiService: GeminiAiService,
    private val cloudRepository: CloudSyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: String = savedStateHandle["noteId"] ?: throw IllegalArgumentException("Note ID required")
    private val json = Json { ignoreUnknownKeys = true }

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

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
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
        }
    }

    fun generateAiContent(instructions: String? = null) {
        if (_title.value.isBlank()) return
        
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val fullPrompt = if (instructions.isNullOrBlank()) _title.value else "${_title.value} ($instructions)"
                val aiBlocks = geminiService.generateNoteContent(fullPrompt)
                if (aiBlocks.isNotEmpty()) {
                    val blocksWithTabs = aiBlocks.map { it.copy(tabName = _selectedTab.value) }
                    _blocks.update { current -> current + blocksWithTabs }
                }
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
            try {
                val newBlock = geminiService.refineBlock(
                    topic = _title.value,
                    blockType = block.type,
                    instructions = block.aiInstructions
                )
                
                if (newBlock != null) {
                    updateBlock(index, newBlock.copy(
                        tabName = block.tabName, 
                        aiInstructions = block.aiInstructions 
                    ))
                }
            } finally {
                _isAiLoading.value = false
            }
        }
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
        }
    }
}
