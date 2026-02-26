package com.algorithmx.medicine101.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.algorithmx.medicine101.ui.screens.noteeditview.NoteEditScreen
import com.algorithmx.medicine101.ui.screens.noteview.NoteViewContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToNote: (String) -> Unit
) {
    val isEditing by viewModel.isEditing.collectAsState()

    if (isEditing) {
        NoteEditScreen(
            viewModel = hiltViewModel(),
            onBack = {
                viewModel.toggleEditMode()
            }
        )
    } else {
        NoteViewContent(
            viewModel = viewModel,
            onNoteLinkClick = onNavigateToNote,
            onBack = onBack
        )
    }
}
