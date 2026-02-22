package com.algorithmx.medicine101.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: String = savedStateHandle["noteId"] ?: throw IllegalArgumentException("Note ID required")
    private val gson = Gson()

    // UI States
    private val _blocks = MutableStateFlow<List<ContentBlock>>(emptyList())
    val blocks: StateFlow<List<ContentBlock>> = _blocks.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _selectedTab = MutableStateFlow("General")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            // 1. Fetch the Note and its related blocks using the new Relation
            val noteWithBlocks = repository.getNoteWithBlocks(noteId)

            if (noteWithBlocks != null) {
                _title.value = noteWithBlocks.note.title

                // 2. Map the separate database rows back into UI blocks
                val uiBlocks = noteWithBlocks.blocks
                    .sortedBy { it.orderIndex }
                    .map { entity ->
                        val type = object : TypeToken<ContentBlock>() {}.type
                        val parsedBlock = gson.fromJson<ContentBlock>(entity.content, type)
                        // Ensure the parsed block gets the tabName from the database row
                        parsedBlock.copy(tabName = entity.tabName)
                    }

                _blocks.value = uiBlocks

                // Automatically set the selected tab to the first available tab in this note
                val firstTab = uiBlocks.firstOrNull()?.tabName ?: "General"
                _selectedTab.value = firstTab
            }
        }
    }

    fun toggleEditMode() {
        _isEditing.update { !it }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    // --- TAB OPERATIONS ---

    fun selectTab(tabName: String) {
        _selectedTab.value = tabName
    }

    // THE GHOST BLOCK CREATOR
    fun addNewTab(newTabName: String) {
        // Create an empty, invisible or basic text block assigned to this new tab
        val ghostBlock = ContentBlock(
            type = "text", // Use a basic text paragraph
            text = "",     // Keep it empty so it looks like a blank canvas
            tabName = newTabName
        )

        _blocks.update { currentList ->
            currentList + ghostBlock
        }
        // Automatically switch to the newly created tab
        selectTab(newTabName)
    }


    // --- BLOCK OPERATIONS ---

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
                // Adjust toIndex if moving downwards to account for the removed item
                val adjustedToIndex = if (toIndex > fromIndex) toIndex - 1 else toIndex
                mutableList.add(adjustedToIndex, item)
            }
            mutableList
        }
    }

    fun addBlock(block: ContentBlock) {
        _blocks.update { currentList ->
            // Ensure the block gets assigned to the currently selected tab
            val blockWithTab = block.copy(tabName = _selectedTab.value)
            currentList + blockWithTab
        }
    }

    // --- SAVE ---

    fun saveNote() {
        viewModelScope.launch {
            val noteWithBlocks = repository.getNoteWithBlocks(noteId)

            if (noteWithBlocks != null) {
                // 1. Update the parent note entity
                val updatedNote = noteWithBlocks.note.copy(
                    title = _title.value,
                    updatedAt = System.currentTimeMillis()
                    // contentJson is no longer stored in the parent note
                )
                repository.updateNote(updatedNote)

                // 2. Convert UI blocks back to Database Entities
                val blockEntities = _blocks.value.mapIndexed { index, uiBlock ->
                    ContentBlockEntity(
                        noteId = noteId,
                        type = uiBlock.type,
                        content = gson.toJson(uiBlock), // Serialize individual block
                        orderIndex = index,
                        tabName = uiBlock.tabName // Save the tab name
                    )
                }

                // 3. Sync blocks to the database
                repository.syncBlocks(noteId, blockEntities)

                _isEditing.value = false // Exit edit mode after save
            }
        }
    }
}