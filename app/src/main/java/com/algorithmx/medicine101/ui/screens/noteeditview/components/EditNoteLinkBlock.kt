package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.ui.screens.folders.ExplorerViewModel

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

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Link to Medical Note", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Handbook...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text(block.linkedNoteTitle ?: "Type note name...") }
        )

        if (filteredNotes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).heightIn(max = 200.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                LazyColumn {
                    items(filteredNotes) { note ->
                        ListItem(
                            headlineContent = { Text(note.title) },
                            supportingContent = { Text(note.category) },
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
        
        if (block.linkedNoteId != null && block.linkedNoteId.isNotEmpty()) {
            AssistChip(
                onClick = { },
                label = { Text("Linked: ${block.linkedNoteTitle}") },
                modifier = Modifier.padding(top = 8.dp),
                trailingIcon = {
                    IconButton(onClick = { onUpdate(block.copy(linkedNoteId = null, linkedNoteTitle = null)) }, modifier = Modifier.size(16.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Remove")
                    }
                }
            )
        }
    }
}
