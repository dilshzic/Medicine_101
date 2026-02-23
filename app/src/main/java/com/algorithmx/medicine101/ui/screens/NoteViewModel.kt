package com.algorithmx.medicine101.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.api.GeminiAiService
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.ContentBlockEntity
import com.algorithmx.medicine101.data.NoteRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val geminiService: GeminiAiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: String = savedStateHandle["noteId"] ?: throw IllegalArgumentException("Note ID required")
    private val gson = Gson()

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
                        val type = object : TypeToken<ContentBlock>() {}.type
                        val parsedBlock = gson.fromJson<ContentBlock>(entity.content, type)
                        parsedBlock.copy(tabName = entity.tabName ?: "General")
                    }
                _blocks.value = uiBlocks
                val firstTab = uiBlocks.firstOrNull()?.tabName ?: "General"
                _selectedTab.value = firstTab
            }
        }
    }

    fun generateAiContent() {
        if (_title.value.isBlank()) return
        
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val aiBlocks = geminiService.generateNoteContent(_title.value)
                if (aiBlocks.isNotEmpty()) {
                    val blocksWithTabs = aiBlocks.map { it.copy(tabName = _selectedTab.value) }
                    _blocks.update { current -> current + blocksWithTabs }
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
                val updatedNote = noteWithBlocks.note.copy(
                    title = _title.value,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateNote(updatedNote)

                val blockEntities = _blocks.value.mapIndexed { index, uiBlock ->
                    ContentBlockEntity(
                        noteId = noteId,
                        type = uiBlock.type,
                        content = gson.toJson(uiBlock),
                        orderIndex = index,
                        tabName = uiBlock.tabName
                    )
                }
                repository.syncBlocks(noteId, blockEntities)
                _isEditing.value = false
            }
        }
    }
}