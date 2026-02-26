package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.ui.screens.folders.ExplorerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteLinkBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit,
    viewModel: ExplorerViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val allNotes by viewModel.items.collectAsState()
    
    val filteredNotes = remember(searchQuery, allNotes) {
        if (searchQuery.isBlank()) emptyList()
        else allNotes.filter { it.title.contains(searchQuery, ignoreCase = true) && !it.isFolder }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = "Medical Reference Link",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Handbook") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            placeholder = { Text(block.linkedNoteTitle ?: "Type note name...") },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (filteredNotes.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .heightIn(max = 240.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                LazyColumn {
                    items(filteredNotes) { note ->
                        ListItem(
                            headlineContent = { Text(note.title) },
                            supportingContent = { Text(note.category) },
                            leadingContent = { Icon(Icons.Default.Link, contentDescription = null) },
                            modifier = Modifier.clickable {
                                onUpdate(block.copy(
                                    linkedNoteId = note.id,
                                    linkedNoteTitle = note.title
                                ))
                                searchQuery = ""
                            }
                        )
                    }
                }
            }
        }
        
        if (!block.linkedNoteId.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            InputChip(
                selected = true,
                onClick = { /* Could navigate to note */ },
                label = { Text(block.linkedNoteTitle ?: "Linked Note") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(InputChipDefaults.IconSize)
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove link",
                        modifier = Modifier
                            .size(InputChipDefaults.IconSize)
                            .clickable {
                                onUpdate(block.copy(linkedNoteId = null, linkedNoteTitle = null))
                            }
                    )
                }
            )
        }
    }
}
