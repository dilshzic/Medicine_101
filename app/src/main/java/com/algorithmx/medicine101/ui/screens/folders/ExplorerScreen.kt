package com.algorithmx.medicine101.ui.screens.folders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.algorithmx.medicine101.ui.screens.folders.components.FolderRow
import com.algorithmx.medicine101.ui.screens.folders.components.NoteRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    viewModel: ExplorerViewModel = hiltViewModel(),
    onFolderClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val title by viewModel.currentTitle.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var isCreatingFolder by remember { mutableStateOf(false) } // False = Note, True = Folder

    // --- NEW: PDF Picker Launcher ---
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.importPdf(it)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // --- NEW: Import PDF Button ---
                    IconButton(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Import PDF"
                        )
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No items found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    if (item.isFolder) {
                        FolderRow(item = item, onClick = { onFolderClick(item.id) })
                    } else {
                        NoteRow(item = item, onClick = { onNoteClick(item.id) })
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = {
                    showCreateDialog = false
                    newItemName = ""
                },
                title = {
                    Text(if (isCreatingFolder) "Create New Folder" else "Create New Note")
                },
                text = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = !isCreatingFolder,
                                    onClick = { isCreatingFolder = false }
                                )
                                Text("Note")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isCreatingFolder,
                                    onClick = { isCreatingFolder = true }
                                )
                                Text("Folder")
                            }
                        }

                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            label = { Text("Name") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newItemName.isNotBlank()) {
                            if (isCreatingFolder) {
                                viewModel.createNewFolder(newItemName)
                            } else {
                                viewModel.createNewNote(newItemName) { newNoteId ->
                                    onNoteClick(newNoteId)
                                }
                            }
                            showCreateDialog = false
                            newItemName = ""
                        }
                    }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCreateDialog = false
                        newItemName = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}