package com.algorithmx.medicine101.ui.screens.folders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.ui.screens.folders.components.FolderRow
import com.algorithmx.medicine101.ui.screens.folders.components.NoteRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerScreen(
    viewModel: ExplorerViewModel = hiltViewModel(),
    onFolderClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onBack: () -> Unit
) {
    val items by viewModel.items.collectAsState()
    val title by viewModel.currentTitle.collectAsState()
    val availableFolders by viewModel.availableFolders.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var isCreatingFolder by remember { mutableStateOf(false) }

    var itemToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var itemToMove by remember { mutableStateOf<NoteEntity?>(null) }
    var itemToRename by remember { mutableStateOf<NoteEntity?>(null) }
    var renameNameInput by remember { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.importPdf(it) }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "Import PDF")
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
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
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No items found", color = Color.Gray)
            }
        } else {
            LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
                items(items) { item ->
                    if (item.isFolder) {
                        FolderRow(
                            item = item,
                            onClick = { onFolderClick(item.id) },
                            onRename = { 
                                itemToRename = item
                                renameNameInput = item.title
                            },
                            onDelete = { itemToDelete = item },
                            onMove = {
                                viewModel.loadAvailableFolders(item.id)
                                itemToMove = item
                            }
                        )
                    } else {
                        NoteRow(
                            item = item,
                            onClick = { onNoteClick(item.id) },
                            onRename = {
                                itemToRename = item
                                renameNameInput = item.title
                            },
                            onDelete = { itemToDelete = item },
                            onMove = {
                                viewModel.loadAvailableFolders("")
                                itemToMove = item
                            }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }
        }

        // Rename Dialog
        if (itemToRename != null) {
            AlertDialog(
                onDismissRequest = { itemToRename = null },
                title = { Text("Rename ${if (itemToRename?.isFolder == true) "Folder" else "Note"}") },
                text = {
                    OutlinedTextField(
                        value = renameNameInput,
                        onValueChange = { renameNameInput = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (renameNameInput.isNotBlank()) {
                            viewModel.renameItem(itemToRename!!.id, renameNameInput)
                            itemToRename = null
                        }
                    }) { Text("Rename") }
                },
                dismissButton = { TextButton(onClick = { itemToRename = null }) { Text("Cancel") } }
            )
        }

        // Create Dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false; newItemName = "" },
                title = { Text(if (isCreatingFolder) "Create New Folder" else "Create New Note") },
                text = {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = !isCreatingFolder, onClick = { isCreatingFolder = false })
                                Text("Note")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = isCreatingFolder, onClick = { isCreatingFolder = true })
                                Text("Folder")
                            }
                        }
                        OutlinedTextField(value = newItemName, onValueChange = { newItemName = it }, label = { Text("Name") }, singleLine = true)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newItemName.isNotBlank()) {
                            if (isCreatingFolder) viewModel.createNewFolder(newItemName)
                            else viewModel.createNewNote(newItemName) { onNoteClick(it) }
                            showCreateDialog = false; newItemName = ""
                        }
                    }) { Text("Create") }
                },
                dismissButton = { TextButton(onClick = { showCreateDialog = false; newItemName = "" }) { Text("Cancel") } }
            )
        }

        // Delete Dialog
        if (itemToDelete != null) {
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Delete ${if (itemToDelete?.isFolder == true) "Folder" else "Note"}?") },
                text = { Text("Are you sure you want to delete '${itemToDelete?.title}'? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            itemToDelete?.let { viewModel.deleteItem(it.id) }
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                },
                dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("Cancel") } }
            )
        }

        // Move Dialog
        if (itemToMove != null) {
            AlertDialog(
                onDismissRequest = { itemToMove = null },
                title = { Text("Move '${itemToMove?.title}'") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        Text("Select destination folder:", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn {
                            item {
                                ListItem(
                                    headlineContent = { Text("Root (Home)") },
                                    leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        viewModel.moveItem(itemToMove!!.id, null)
                                        itemToMove = null
                                    }
                                )
                            }
                            items(availableFolders) { folder ->
                                ListItem(
                                    headlineContent = { Text(folder.title) },
                                    leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        viewModel.moveItem(itemToMove!!.id, folder.id)
                                        itemToMove = null
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { itemToMove = null }) { Text("Cancel") } }
            )
        }
    }
}
