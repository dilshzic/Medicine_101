package com.algorithmx.medicine101.ui.screens.folders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = { 
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
            FloatingActionButton(onClick = { /* TODO: Add "Create New" Logic later */ }) {
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
    }
}