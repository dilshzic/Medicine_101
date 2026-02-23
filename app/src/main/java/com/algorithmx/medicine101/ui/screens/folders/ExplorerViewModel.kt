package com.algorithmx.medicine101.ui.screens.folders

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import com.algorithmx.medicine101.utils.PdfImportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val pdfImportManager: PdfImportManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val folderId: String? = savedStateHandle.get<String>("folderId")

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

    val currentTitle: StateFlow<String> = flow {
        if (folderId == null) {
            emit("MedMate Notes")
        } else {
            val folder = repository.getNoteById(folderId)
            emit(folder?.title ?: "Folder")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Loading..."
    )

    private val _availableFolders = MutableStateFlow<List<NoteEntity>>(emptyList())
    val availableFolders: StateFlow<List<NoteEntity>> = _availableFolders.asStateFlow()

    fun createNewFolder(folderName: String) {
        if (folderName.isBlank()) return
        viewModelScope.launch {
            val newFolder = NoteEntity(
                id = UUID.randomUUID().toString(),
                title = folderName.trim(),
                category = "User",
                isFolder = true,
                parentId = folderId,
                isSystemNote = false,
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
                isFolder = false,
                parentId = folderId,
                isSystemNote = false,
                tags = ""
            )
            repository.insertNote(newNote)
            onCreated(noteId)
        }
    }

    fun importPdf(uri: Uri) {
        viewModelScope.launch {
            pdfImportManager.importPdf(uri, "Imported Textbook")
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.softDeleteNote(id)
        }
    }

    fun moveItem(id: String, newParentId: String?) {
        viewModelScope.launch {
            repository.moveNote(id, newParentId)
        }
    }

    fun loadAvailableFolders(excludeId: String) {
        viewModelScope.launch {
            val folders = repository.getAllFoldersExcept(excludeId)
            _availableFolders.value = folders
        }
    }

    suspend fun getNoteById(id: String): NoteEntity? {
        return repository.getNoteById(id)
    }
}