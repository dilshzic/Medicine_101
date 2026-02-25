package com.algorithmx.medicine101.ui.screens.noteeditview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.ui.screens.NoteViewModel
import com.algorithmx.medicine101.ui.screens.noteeditview.components.*
import com.algorithmx.medicine101.ui.screens.noteview.components.RenderSingleBlock
import com.algorithmx.medicine101.flowchart.ui.FlowchartScreen

@Composable
fun NoteEditScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val blocks by viewModel.blocks.collectAsState()
    val title by viewModel.title.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var editingBlockIndex by remember { mutableIntStateOf(-1) }
    var isDrawingFlowchart by remember { mutableStateOf(false) }

    if (isDrawingFlowchart && editingBlockIndex != -1) {
        val block = blocks[editingBlockIndex]
        FlowchartScreen(
            initialData = block.flowchart,
            onDone = { 
                viewModel.updateBlock(editingBlockIndex, block.copy(flowchart = it))
                isDrawingFlowchart = false
            },
            onBack = { isDrawingFlowchart = false }
        )
    } else if (editingBlockIndex != -1) {
        val block = blocks[editingBlockIndex]
        BlockEditScreen(
            block = block,
            isAiLoading = isAiLoading,
            onUpdate = { viewModel.updateBlock(editingBlockIndex, it) },
            onRefineAi = { viewModel.refineBlockWithAi(editingBlockIndex) },
            onEditChart = { isDrawingFlowchart = true },
            onBack = { editingBlockIndex = -1 }
        )
    } else {
        NoteEditContent(
            title = title,
            blocks = blocks,
            selectedTab = selectedTab,
            isAiLoading = isAiLoading,
            onTitleChange = viewModel::updateTitle,
            onTabSelected = viewModel::selectTab,
            onNewTab = viewModel::addNewTab,
            onRenameTab = viewModel::renameTab,
            onSave = viewModel::saveNote,
            onAddBlock = viewModel::addBlock,
            onDeleteBlock = viewModel::deleteBlock,
            onMoveBlockUp = { index -> viewModel.moveBlock(index, index - 1) },
            onMoveBlockDown = { index -> viewModel.moveBlock(index, index + 1) },
            onUpdateBlock = viewModel::updateBlock,
            onGenerateAi = viewModel::generateAiContent,
            onEditBlock = { editingBlockIndex = it },
            onBack = onBack
        )
    }
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
    onRenameTab: (String, String) -> Unit,
    onSave: () -> Unit,
    onAddBlock: (ContentBlock) -> Unit,
    onDeleteBlock: (Int) -> Unit,
    onMoveBlockUp: (Int) -> Unit,
    onMoveBlockDown: (Int) -> Unit,
    onUpdateBlock: (Int, ContentBlock) -> Unit,
    onGenerateAi: (String?) -> Unit,
    onEditBlock: (Int) -> Unit,
    onBack: () -> Unit
) {
    var showAddBlockSheet by remember { mutableStateOf(false) }
    var showNewTabDialog by remember { mutableStateOf(false) }
    var showRenameTabDialog by remember { mutableStateOf(false) }
    var showAiGlobalDialog by remember { mutableStateOf(false) }
    var globalAiInstructions by remember { mutableStateOf("") }
    
    var newTabNameInput by remember { mutableStateOf("") }
    var renameTabNameInput by remember { mutableStateOf("") }

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
                        modifier = Modifier.fillMaxWidth(0.7f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    if (isAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 16.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { showAiGlobalDialog = true }) {
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
            PrimaryScrollableTabRow(
                selectedTabIndex = availableTabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                availableTabs.forEach { tabName ->
                    Tab(
                        selected = tabName == selectedTab,
                        onClick = { onTabSelected(tabName) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tabName)
                                if (tabName == selectedTab) {
                                    IconButton(
                                        onClick = {
                                            renameTabNameInput = tabName
                                            showRenameTabDialog = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Rename Tab",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
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
                            onEdit = { onEditBlock(globalIndex) },
                            onDelete = { onDeleteBlock(globalIndex) },
                            onMoveUp = { onMoveBlockUp(globalIndex) },
                            onMoveDown = { onMoveBlockDown(globalIndex) }
                        ) {
                            RenderSingleBlock(block)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        if (showAiGlobalDialog) {
            AlertDialog(
                onDismissRequest = { showAiGlobalDialog = false },
                title = { Text("Generate with AI") },
                text = {
                    OutlinedTextField(
                        value = globalAiInstructions,
                        onValueChange = { globalAiInstructions = it },
                        label = { Text("Special Instructions (Optional)") },
                        placeholder = { Text("e.g., focus on pathophysiology, make it concise...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        onGenerateAi(globalAiInstructions)
                        showAiGlobalDialog = false
                        globalAiInstructions = ""
                    }) { Text("Generate") }
                },
                dismissButton = {
                    TextButton(onClick = { showAiGlobalDialog = false }) { Text("Cancel") }
                }
            )
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

        if (showRenameTabDialog) {
            AlertDialog(
                onDismissRequest = { showRenameTabDialog = false },
                title = { Text("Rename Tab") },
                text = {
                    OutlinedTextField(
                        value = renameTabNameInput,
                        onValueChange = { renameTabNameInput = it },
                        label = { Text("New Tab Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (renameTabNameInput.isNotBlank()) {
                            onRenameTab(selectedTab, renameTabNameInput.trim())
                            showRenameTabDialog = false
                        }
                    }) { Text("Rename") }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameTabDialog = false }) { Text("Cancel") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockEditScreen(
    block: ContentBlock,
    isAiLoading: Boolean,
    onUpdate: (ContentBlock) -> Unit,
    onRefineAi: () -> Unit,
    onEditChart: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit ${block.type.replaceFirstChar { it.uppercase() }}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 16.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = onRefineAi) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Refine with AI", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = block.aiInstructions ?: "",
                onValueChange = { onUpdate(block.copy(aiInstructions = it)) },
                label = { Text("AI Instructions") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                placeholder = { Text("e.g. make it simpler, focus on treatment...") }
            )

            HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp))

            when (block.type) {
                "header" -> EditHeaderBlock(block, onUpdate)
                "callout" -> EditCalloutBlock(block, onUpdate)
                "list" -> EditListBlock(block, onUpdate)
                "table" -> EditTableBlock(block, onUpdate)
                "dd_table" -> EditDDBlock(block, onUpdate)
                "accordion" -> EditAccordionBlock(block, onUpdate)
                "flowchart" -> EditFlowchartBlock(block, onEditChart, onUpdate)
                "image" -> EditImageBlock(block, onUpdate)
                "youtube" -> EditYouTubeBlock(block, onUpdate)
                "note_link" -> EditNoteLinkBlock(block, onUpdate)
                else -> EditTextBlock(block, "Text Content", onUpdate)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}
