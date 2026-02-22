package com.algorithmx.medicine101.ui.screens.folders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get the folderId passed from Navigation. It might be null (Root).
    private val folderId: String? = savedStateHandle.get<String>("folderId")

    // The List of Items (Folders + Notes)
    val items: StateFlow<List<NoteEntity>> = flow {
        if (folderId == null) {
            emitAll(repository.getRootItems())
        } else {
            emitAll(repository.getItemsInFolder(folderId))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // The Title of the current screen
    val currentTitle: StateFlow<String> = flow {
        if (folderId == null) {
            emit("MedMate Notes")
        } else {
            // If inside a folder, fetch the folder's name to show in TopBar
            val folder = repository.getNoteById(folderId)
            emit(folder?.title ?: "Folder")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = "Loading..."
    )
    fun createNewFolder(folderName: String) {
        if (folderName.isBlank()) return

        viewModelScope.launch {
            val newFolder = NoteEntity(
                id = UUID.randomUUID().toString(),
                title = folderName.trim(),
                category = "User", // Tag it as a user-created item
                isFolder = true,
                parentId = folderId, // This puts the folder inside the current view (or root if null)
                isSystemNote = false, // Not a pre-seeded system note
                tags = "" // Add any other default fields your NoteEntity requires
            )
            repository.insertNote(newFolder)
        }
    }
}