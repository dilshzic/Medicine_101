package com.algorithmx.medicine101.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.NoteRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            val note = repository.getNoteById(noteId)
            if (note != null) {
                _title.value = note.title
                
                // Deserialize JSON to List<ContentBlock>
                if (!note.contentJson.isNullOrEmpty()) {
                    val type = object : TypeToken<List<ContentBlock>>() {}.type
                    _blocks.value = gson.fromJson(note.contentJson, type)
                }
            }
        }
    }

    fun toggleEditMode() {
        _isEditing.value = !_isEditing.value
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    // --- BLOCK OPERATIONS ---

    fun updateBlock(index: Int, newBlock: ContentBlock) {
        val currentList = _blocks.value.toMutableList()
        currentList[index] = newBlock
        _blocks.value = currentList
    }

    fun deleteBlock(index: Int) {
        val currentList = _blocks.value.toMutableList()
        currentList.removeAt(index)
        _blocks.value = currentList
    }

    fun moveBlock(fromIndex: Int, toIndex: Int) {
        val currentList = _blocks.value.toMutableList()
        if (toIndex in 0 until currentList.size) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            _blocks.value = currentList
        }
    }

    fun addBlock(block: ContentBlock) {
        val currentList = _blocks.value.toMutableList()
        currentList.add(block)
        _blocks.value = currentList
    }

    // --- SAVE ---
    
    fun saveNote() {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                // Serialize List -> JSON
                val jsonString = gson.toJson(_blocks.value)
                
                val updatedNote = note.copy(
                    title = _title.value,
                    contentJson = jsonString,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateNote(updatedNote)
                _isEditing.value = false // Exit edit mode after save
            }
        }
    }
}