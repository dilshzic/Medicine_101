package com.algorithmx.medicine101.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.algorithmx.medicine101.ui.screens.noteeditview.NoteEditContent
import com.algorithmx.medicine101.ui.screens.noteview.NoteViewContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: EditorViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val isEditing by viewModel.isEditing.collectAsState()

    // The Host Screen simply decides which UI to show
    if (isEditing) {
        NoteEditContent(viewModel = viewModel, onBack = onBack)
    } else {
        NoteViewContent(viewModel = viewModel, onBack = onBack)
    }
}