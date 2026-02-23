package com.algorithmx.medicine101.ui.screens.noteeditview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.ui.screens.NoteViewModel
import com.algorithmx.medicine101.ui.screens.noteeditview.components.*
import com.algorithmx.medmate.screens.editor.EditTableBlock

@Composable
fun NoteEditScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val blocks by viewModel.blocks.collectAsState()
    val title by viewModel.title.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    NoteEditContent(
        title = title,
        blocks = blocks,
        selectedTab = selectedTab,
        isAiLoading = isAiLoading,
        onTitleChange = viewModel::updateTitle,
        onTabSelected = viewModel::selectTab,
        onNewTab = viewModel::addNewTab,
        onSave = viewModel::saveNote,
        onAddBlock = viewModel::addBlock,
        onDeleteBlock = viewModel::deleteBlock,
        onMoveBlockUp = { index -> viewModel.moveBlock(index, index - 1) },
        onMoveBlockDown = { index -> viewModel.moveBlock(index, index + 1) },
        onUpdateBlock = viewModel::updateBlock,
        onGenerateAi = viewModel::generateAiContent,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditContent(
    title: String,
    blocks: List<ContentBlock>,
    selectedTab: String,
    isAiLoading: Boolean,
    onTitleChange: (String) -> Unit,
    onTabSelected: (String) -> Unit,
    onNewTab: (String) -> Unit,
    onSave: () -> Unit,
    onAddBlock: (ContentBlock) -> Unit,
    onDeleteBlock: (Int) -> Unit,
    onMoveBlockUp: (Int) -> Unit,
    onMoveBlockDown: (Int) -> Unit,
    onUpdateBlock: (Int, ContentBlock) -> Unit,
    onGenerateAi: () -> Unit,
    onBack: () -> Unit
) {
    var showAddBlockSheet by remember { mutableStateOf(false) }
    var showNewTabDialog by remember { mutableStateOf(false) }
    var newTabNameInput by remember { mutableStateOf("") }

    val availableTabs = blocks.map { it.tabName }.distinct().ifEmpty { listOf("General") }
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
                    if (isAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 16.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = onGenerateAi) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Generate AI Content", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
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
            modifier = Modifier.padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = availableTabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                availableTabs.forEach { tabName ->
                    Tab(
                        selected = tabName == selectedTab,
                        onClick = { onTabSelected(tabName) },
                        text = { Text(tabName) }
                    )
                }
                Tab(
                    selected = false,
                    onClick = { showNewTabDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "New Tab") }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                itemsIndexed(currentTabBlocks) { _, block ->
                    val globalIndex = blocks.indexOf(block)
                    if (globalIndex != -1) {
                        BlockWrapper(
                            onDelete = { onDeleteBlock(globalIndex) },
                            onMoveUp = { onMoveBlockUp(globalIndex) },
                            onMoveDown = { onMoveBlockDown(globalIndex) }
                        ) {
                            when (block.type) {
                                "header" -> EditHeaderBlock(block) { onUpdateBlock(globalIndex, it) }
                                "callout" -> EditCalloutBlock(block) { onUpdateBlock(globalIndex, it) }
                                "list" -> EditListBlock(block) { onUpdateBlock(globalIndex, it) }
                                "table" -> EditTableBlock(block) { onUpdateBlock(globalIndex, it) }
                                "dd_table" -> EditDDBlock(block) { onUpdateBlock(globalIndex, it) }
                                "accordion" -> EditAccordionBlock(block) { onUpdateBlock(globalIndex, it) }
                                "flowchart" -> EditFlowchartBlock(block) { onUpdateBlock(globalIndex, it) }
                                "image" -> EditImageBlock(block) { onUpdateBlock(globalIndex, it) }
                                "youtube" -> EditYouTubeBlock(block) { onUpdateBlock(globalIndex, it) }
                                else -> EditTextBlock(block, "Text Content") { onUpdateBlock(globalIndex, it) }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (currentTabBlocks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("This tab is empty", color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = onGenerateAi, enabled = !isAiLoading) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate with AI")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showNewTabDialog) {
            AlertDialog(
                onDismissRequest = { showNewTabDialog = false },
                title = { Text("Create New Tab") },
                text = {
                    OutlinedTextField(
                        value = newTabNameInput,
                        onValueChange = { newTabNameInput = it },
                        label = { Text("Tab Name") },
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
                    val blockWithTab = newBlock.copy(tabName = selectedTab)
                    onAddBlock(blockWithTab)
                    showAddBlockSheet = false
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NoteEditContentPreview() {
    MaterialTheme {
        NoteEditContent(
            title = "Hypertension",
            blocks = emptyList(),
            selectedTab = "General",
            isAiLoading = false,
            onTitleChange = {},
            onTabSelected = {},
            onNewTab = {},
            onSave = {},
            onAddBlock = {},
            onDeleteBlock = {},
            onMoveBlockUp = {},
            onMoveBlockDown = {},
            onUpdateBlock = { _, _ -> },
            onGenerateAi = {},
            onBack = {}
        )
    }
}