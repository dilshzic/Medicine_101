package com.algorithmx.medicine101.ui.screens.noteeditview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.ui.screens.NoteViewModel

import com.algorithmx.medicine101.ui.screens.noteeditview.components.BlockCreationSheet
import com.algorithmx.medicine101.ui.screens.noteeditview.components.BlockWrapper
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditAccordionBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditCalloutBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditDDBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditFlowchartBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditHeaderBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditImageBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditListBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditTextBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditYouTubeBlock
import com.algorithmx.medmate.screens.editor.EditTableBlock

// 1. STATEFUL WRAPPER: Handles the ViewModel logic
@Composable
fun NoteEditScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val blocks by viewModel.blocks.collectAsState()
    val title by viewModel.title.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    NoteEditContent(
        title = title,
        blocks = blocks,
        selectedTab = selectedTab,
        onTitleChange = viewModel::updateTitle,
        onTabSelected = viewModel::selectTab,
        onNewTab = viewModel::addNewTab,
        onSave = viewModel::saveNote,
        onAddBlock = viewModel::addBlock,
        onDeleteBlock = viewModel::deleteBlock,
        onMoveBlockUp = { index -> viewModel.moveBlock(index, index - 1) },
        onMoveBlockDown = { index -> viewModel.moveBlock(index, index + 1) },
        onUpdateBlock = viewModel::updateBlock,
        onBack = onBack
    )
}

// 2. STATELESS UI: Pure Compose, easily previewable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditContent(
    title: String,
    blocks: List<ContentBlock>,
    selectedTab: String,
    onTitleChange: (String) -> Unit,
    onTabSelected: (String) -> Unit,
    onNewTab: (String) -> Unit,
    onSave: () -> Unit,
    onAddBlock: (ContentBlock) -> Unit,
    onDeleteBlock: (Int) -> Unit,
    onMoveBlockUp: (Int) -> Unit,
    onMoveBlockDown: (Int) -> Unit,
    onUpdateBlock: (Int, ContentBlock) -> Unit,
    onBack: () -> Unit
) {
    var showAddBlockSheet by remember { mutableStateOf(false) }
    var showNewTabDialog by remember { mutableStateOf(false) }
    var newTabNameInput by remember { mutableStateOf("") }

    // Determine available tabs based on the blocks. Fallback to "General" if completely empty.
    val availableTabs = blocks.map { it.tabName }.distinct().ifEmpty { listOf("General") }

    // Filter the blocks that belong to the currently selected tab
    val currentTabBlocks = blocks.filter { it.tabName == selectedTab }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                },
                actions = {
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddBlockSheet = true }) {
                Icon(Icons.Default.Add, "Add Block")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            // --- THE TAB ROW ---
            ScrollableTabRow(
                selectedTabIndex = availableTabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Render existing tabs
                availableTabs.forEach { tabName ->
                    Tab(
                        selected = tabName == selectedTab,
                        onClick = { onTabSelected(tabName) },
                        text = { Text(tabName) }
                    )
                }
                // Add a "+" tab to create new tabs
                Tab(
                    selected = false,
                    onClick = { showNewTabDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "New Tab") }
                )
            }

            // --- THE FILTERED LAZY COLUMN ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Iterate ONLY over the blocks that belong to the selected tab
                itemsIndexed(currentTabBlocks) { _, block ->

                    // CRITICAL: We need the global index from the master list
                    // so operations apply to the right item in the ViewModel
                    val globalIndex = blocks.indexOf(block)

                    if (globalIndex != -1) {
                        BlockWrapper(
                            onDelete = { onDeleteBlock(globalIndex) },
                            onMoveUp = { onMoveBlockUp(globalIndex) },
                            onMoveDown = { onMoveBlockDown(globalIndex) }
                        ) {
                            when (block.type) {
                                "header" -> EditHeaderBlock(block) { onUpdateBlock(globalIndex, it) }

                                // --- UPDATED: Use the new dedicated callout editor ---
                                "callout" -> EditCalloutBlock(block) { onUpdateBlock(globalIndex, it) }

                                "list" -> EditListBlock(block) { onUpdateBlock(globalIndex, it) }
                                "table" -> EditTableBlock(block) { onUpdateBlock(globalIndex, it) }
                                "dd_table" -> EditDDBlock(block) { onUpdateBlock(globalIndex, it) }

                                // --- NEW: Handle complex blocks gracefully ---
                                // --- INTEGRATE THE NEW EDITORS ---
                                "accordion" -> EditAccordionBlock(block) { onUpdateBlock(globalIndex, it) }
                                "flowchart" -> EditFlowchartBlock(block) { onUpdateBlock(globalIndex, it) }
                                "image" -> EditImageBlock(block) { onUpdateBlock(globalIndex, it) }
                                // ... inside the LazyColumn ...
                                "youtube" -> EditYouTubeBlock(block) { onUpdateBlock(globalIndex, it) }

                                else -> EditTextBlock(block, "Text Content") { onUpdateBlock(globalIndex, it) }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            onAddBlock(ContentBlock(type = "callout", text = "New Block", tabName = selectedTab))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 80.dp) // Extra padding at bottom for FAB
                    ) {
                        Text("Add to $selectedTab")
                    }
                }
            }
        }

        // --- NEW TAB DIALOG ---
        if (showNewTabDialog) {
            AlertDialog(
                onDismissRequest = { showNewTabDialog = false },
                title = { Text("Create New Tab") },
                text = {
                    OutlinedTextField(
                        value = newTabNameInput,
                        onValueChange = { newTabNameInput = it },
                        label = { Text("Tab Name (e.g., Pathophysiology)") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newTabNameInput.isNotBlank()) {
                            onNewTab(newTabNameInput.trim())
                            newTabNameInput = ""
                            showNewTabDialog = false
                        }
                    }) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showNewTabDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showAddBlockSheet) {
            BlockCreationSheet(
                onDismiss = { showAddBlockSheet = false },
                onBlockSelected = { newBlock ->
                    // Make sure the block being added is assigned to the current tab
                    val blockWithTab = newBlock.copy(tabName = selectedTab)
                    onAddBlock(blockWithTab)
                    showAddBlockSheet = false
                }
            )
        }
    }
}

// 3. THE PREVIEW
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NoteEditContentPreview() {
    MaterialTheme {
        NoteEditContent(
            title = "Hypertension Guidelines",
            blocks = listOf(
                ContentBlock(type = "header", text = "Diagnostic Criteria", tabName = "Diagnosis"),
                ContentBlock(type = "callout", text = "BP > 140/90 mmHg requires intervention", tabName = "Management"),
                ContentBlock(type = "text", text = "First line treatment includes ACE inhibitors or ARBs.", tabName = "Management")
            ),
            selectedTab = "Management",
            onTitleChange = {},
            onTabSelected = {},
            onNewTab = {},
            onSave = {},
            onAddBlock = {},
            onDeleteBlock = {},
            onMoveBlockUp = {},
            onMoveBlockDown = {},
            onUpdateBlock = { _, _ -> },
            onBack = {}
        )
    }
}