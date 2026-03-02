package com.algorithmx.medicine101.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.algorithmx.medicine101.data.NoteEntity


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNoteClick: (String) -> Unit,
    onFolderClick: (String) -> Unit = {}
) {
    val query by viewModel.query.collectAsState()
    val categorizedResults by viewModel.categorizedResults.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Search diagnosis, labs, books...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp)
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            if (query.isEmpty()) {
                item { SearchEmptyState() }
            } else if (categorizedResults.notes.isEmpty() && categorizedResults.pdfNodes.isEmpty() && categorizedResults.findings.isEmpty()) {
                item { NoResultsState(query) }
            } else {
                // 1. PDF / Reference Books Section
                if (categorizedResults.pdfNodes.isNotEmpty()) {
                    stickyHeader { SearchSectionHeader("Reference Library") }
                    items(categorizedResults.pdfNodes) { item ->
                        SearchPdfResultItem(item, onClick = { onNoteClick(item.id) })
                    }
                }

                // 2. Clinical Notes Section
                if (categorizedResults.notes.isNotEmpty()) {
                    stickyHeader { SearchSectionHeader("Clinical Notes") }
                    items(categorizedResults.notes) { item ->
                        SearchResultItem(item, onClick = { onNoteClick(item.id) })
                    }
                }

                // 3. Findings Section
                if (categorizedResults.findings.isNotEmpty()) {
                    stickyHeader { SearchSectionHeader("Internal Findings") }
                    items(categorizedResults.findings) { item ->
                        SearchFindingResultItem(item, onClick = { onNoteClick(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SearchPdfResultItem(item: NoteEntity, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Icon(
                    Icons.Default.AutoStories,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        headlineContent = { Text(item.title, fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.width(4.dp))
                Text("Reference Book", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}

@Composable
fun SearchResultItem(item: NoteEntity, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        headlineContent = { Text(item.title, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Text(item.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        }
    )
}

@Composable
fun SearchFindingResultItem(item: NoteEntity, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = {
            Icon(Icons.Default.TableChart, null, tint = MaterialTheme.colorScheme.tertiary)
        },
        headlineContent = { Text("Match found in contents", style = MaterialTheme.typography.bodyMedium) },
        supportingContent = { Text("Note: ${item.title}", style = MaterialTheme.typography.labelSmall) }
    )
}

@Composable
fun SearchEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Search, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Text("Search Clinical Database", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
        Text("Find notes, textbook references, and internal findings instantly.", 
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    }
}

@Composable
fun NoResultsState(query: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))
        Text("No results for \"$query\"", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
        Text("Try checking your spelling or using more general medical terms.", 
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    }
}
