package com.algorithmx.medicine101.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import com.algorithmx.medicine101.data.remote.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    val pinnedNotes: StateFlow<List<NoteEntity>> = repository.getPinnedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentNotes: StateFlow<List<NoteEntity>> = repository.getRecentNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        syncData()
    }

    fun syncData() {
        viewModelScope.launch {
            _isSyncing.value = true
            syncManager.syncEverything()
            _isSyncing.value = false
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.updatePinStatus(note.id, !note.isPinned)
        }
    }
}