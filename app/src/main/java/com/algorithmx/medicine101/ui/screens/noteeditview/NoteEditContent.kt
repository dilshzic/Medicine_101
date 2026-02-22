package com.algorithmx.medicine101.ui.screens.noteeditview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.ui.screens.EditorViewModel
import com.algorithmx.medicine101.ui.screens.noteeditview.components.BlockCreationSheet
import com.algorithmx.medicine101.ui.screens.noteeditview.components.BlockWrapper
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditDDBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditHeaderBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditListBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditTextBlock
import com.algorithmx.medmate.screens.editor.EditTableBlock

// 1. STATEFUL WRAPPER: Handles the ViewModel logic
@Composable
fun NoteEditScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    val blocks by viewModel.blocks.collectAsState()
    val title by viewModel.title.collectAsState()

    NoteEditContent(
        title = title,
        blocks = blocks,
        onTitleChange = viewModel::updateTitle,
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
    onTitleChange: (String) -> Unit,
    onSave: () -> Unit,
    onAddBlock: (ContentBlock) -> Unit,
    onDeleteBlock: (Int) -> Unit,
    onMoveBlockUp: (Int) -> Unit,
    onMoveBlockDown: (Int) -> Unit,
    onUpdateBlock: (Int, ContentBlock) -> Unit,
    onBack: () -> Unit
) {
    var showAddBlockSheet by remember { mutableStateOf(false) }

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
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(blocks) { index, block ->
                    BlockWrapper(
                        onDelete = { onDeleteBlock(index) },
                        onMoveUp = { onMoveBlockUp(index) },
                        onMoveDown = { onMoveBlockDown(index) }
                    ) {
                        when (block.type) {
                            "header" -> EditHeaderBlock(block) { onUpdateBlock(index, it) }
                            "callout" -> EditTextBlock(block, "Callout Text") { onUpdateBlock(index, it) }
                            "list" -> EditListBlock(block) { onUpdateBlock(index, it) }
                            "table" -> EditTableBlock(block) { onUpdateBlock(index, it) }
                            "dd_table" -> EditDDBlock(block) { onUpdateBlock(index, it) }
                            else -> EditTextBlock(block, "Text Content") { onUpdateBlock(index, it) }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { onAddBlock(ContentBlock(type = "callout", text = "New Block")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 32.dp)
                    ) {
                        Text("Add Paragraph")
                    }
                }
            }
        }

        if (showAddBlockSheet) {
            BlockCreationSheet(
                onDismiss = { showAddBlockSheet = false },
                onBlockSelected = { newBlock ->
                    onAddBlock(newBlock)
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
                ContentBlock(type = "header", text = "Diagnostic Criteria"),
                ContentBlock(type = "callout", text = "BP > 140/90 mmHg requires intervention"),
                ContentBlock(type = "text", text = "First line treatment includes ACE inhibitors or ARBs.")
            ),
            onTitleChange = {},
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