package com.algorithmx.medicine101.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel // Recommended Hilt import
import com.algorithmx.medicine101.ui.screens.noteeditview.NoteEditScreen // NEW: Import the Stateful Wrapper
import com.algorithmx.medicine101.ui.screens.noteview.NoteViewContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val isEditing by viewModel.isEditing.collectAsState()

    // The Host Screen decides which UI to show based on NoteViewModel's state
    if (isEditing) {
        // 1. Call the Stateful Wrapper we created (NoteEditScreen, not NoteEditContent)
        // 2. Pass it a fresh EditorViewModel using hiltViewModel()
        NoteEditScreen(
            viewModel = hiltViewModel(),
            onBack = {
                // Toggle edit mode off to return to View Mode instead of leaving the screen entirely
                viewModel.toggleEditMode()
            }
        )
    } else {
        // NoteViewContent continues to use your standard NoteViewModel
        NoteViewContent(
            viewModel = viewModel,
            onBack = onBack
        )
    }
}