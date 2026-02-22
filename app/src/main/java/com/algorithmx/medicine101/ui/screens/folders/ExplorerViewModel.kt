package com.algorithmx.medicine101.ui.screens.folders

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import com.algorithmx.medicine101.utils.PdfImportManager
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
    private val pdfImportManager: PdfImportManager, // <-- INJECTED HERE
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
        started = SharingStarted.WhileSubscribed(5000),
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
        started = SharingStarted.WhileSubscribed(5000),
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
                tags = ""
            )
            repository.insertNote(newFolder)
        }
    }

    fun createNewNote(noteTitle: String, onCreated: (String) -> Unit) {
        if (noteTitle.isBlank()) return

        viewModelScope.launch {
            val noteId = UUID.randomUUID().toString()
            val newNote = NoteEntity(
                id = noteId,
                title = noteTitle.trim(),
                category = "User",
                isFolder = false, // It's a note, not a folder
                parentId = folderId,
                isSystemNote = false,
                tags = ""
            )
            repository.insertNote(newNote)

            // Trigger the callback with the new ID so the UI can navigate to it
            onCreated(noteId)
        }
    }

    // --- NEW: PDF Import Function ---
    fun importPdf(uri: Uri) {
        viewModelScope.launch {
            // Hardcoding "Imported Textbook" for now. Later you can extract the real
            // file name using Android's ContentResolver if you prefer.
            pdfImportManager.importPdf(uri, "Imported Textbook")
        }
    }

    // In ExplorerViewModel.kt
    suspend fun getNoteById(id: String): NoteEntity? {
        return repository.getNoteById(id)
    }
}