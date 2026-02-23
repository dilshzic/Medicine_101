package com.algorithmx.medicine101.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.ui.screens.folders.components.NoteRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNoteClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onExplorerClick: () -> Unit
) {
    val pinnedNotes by viewModel.pinnedNotes.collectAsState()
    val recentNotes by viewModel.recentNotes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onExplorerClick) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Explorer")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding
        ) {
            // Quick Actions / Stats could go here
            
            // Pinned Section
            if (pinnedNotes.isNotEmpty()) {
                item {
                    SectionHeader(title = "Pinned")
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(pinnedNotes) { note ->
                            PinnedNoteCard(
                                note = note,
                                onClick = { onNoteClick(note.id) },
                                onTogglePin = { viewModel.togglePin(note) }
                            )
                        }
                    }
                }
            }

            // Recent Section
            item {
                SectionHeader(title = "Recent Notes")
            }
            
            if (recentNotes.isNotEmpty()) {
                items(recentNotes) { note ->
                    NoteRow(
                        item = note,
                        onClick = { onNoteClick(note.id) },
                        onDelete = {}, 
                        onMove = {}
                    )
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent notes", color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onExplorerClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse All Folders")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun PinnedNoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 160.dp, height = 100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                modifier = Modifier.align(Alignment.TopStart)
            )
            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.align(Alignment.BottomEnd).size(24.dp)
            ) {
                Icon(
                    imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = "Pin",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}