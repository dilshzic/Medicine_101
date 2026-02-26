package com.algorithmx.medicine101.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.data.remote.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult: StateFlow<String?> = _syncResult.asStateFlow()

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncResult.value = null
            val result = syncManager.syncEverything()
            if (result.isSuccess) {
                _syncResult.value = "Sync successful!"
            } else {
                _syncResult.value = "Sync failed: ${result.exceptionOrNull()?.message}"
            }
            _isSyncing.value = false
        }
    }

    fun clearSyncResult() {
        _syncResult.value = null
    }
}
