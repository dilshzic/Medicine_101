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
class EditorViewModel @Inject constructor(
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

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            // 1. Fetch the Note and its related blocks from the repository
            val noteWithBlocks = repository.getNoteWithBlocks(noteId)

            if (noteWithBlocks != null) {
                // 2. Set the title from the Note entity
                _title.value = noteWithBlocks.note.title

                // 3. Convert the list of entities into List<ContentBlock> for the UI
                val uiBlocks = noteWithBlocks.blocks
                    .sortedBy { it.orderIndex } // Ensure medical notes stay in the correct sequence
                    .map { entity ->
                        val type = object : TypeToken<ContentBlock>() {}.type
                        gson.fromJson<ContentBlock>(entity.content, type)
                    }

                _blocks.value = uiBlocks
            }
        }
    }

    fun toggleEditMode() {
        _isEditing.update { !it }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    // --- BLOCK OPERATIONS (Optimized with .update) ---

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
            currentList + block // Simple and efficient list appending
        }
    }

    // --- SAVE ---

    fun saveNote() {
        viewModelScope.launch {
            // 1. Get the current note to retain its other fields (category, etc.)
            val noteWithBlocks = repository.getNoteWithBlocks(noteId)

            if (noteWithBlocks != null) {
                // 2. Update the parent note entity
                val updatedNote = noteWithBlocks.note.copy(
                    title = _title.value
                    // Remove contentJson here since we don't store it in the NoteEntity anymore
                )
                repository.updateNote(updatedNote)

                // 3. Convert UI blocks back to Database Entities
                val blockEntities = _blocks.value.mapIndexed { index, uiBlock ->
                    ContentBlockEntity(
                        noteId = noteId,
                        type = uiBlock.type,
                        content = gson.toJson(uiBlock), // Serialize the individual block
                        orderIndex = index
                    )
                }

                // 4. Sync blocks to the database
                // (This function should delete old blocks for this noteId and insert the new ones)
                repository.syncBlocks(noteId, blockEntities)

                _isEditing.value = false // Exit edit mode after save
            }
        }
    }
}