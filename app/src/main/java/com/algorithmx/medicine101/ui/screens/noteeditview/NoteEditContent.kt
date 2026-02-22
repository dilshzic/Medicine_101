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
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock

// Make sure to import your block components and ContentBlock data class
import com.algorithmx.medicine101.ui.screens.EditorViewModel
import com.algorithmx.medicine101.ui.screens.noteeditview.components.BlockCreationSheet
import com.algorithmx.medicine101.ui.screens.noteeditview.components.BlockWrapper
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditDDBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditHeaderBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditListBlock
import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditTextBlock
import com.algorithmx.medmate.screens.editor.EditTableBlock

// import com.algorithmx.medicine101.basic.ContentBlock
// import com.algorithmx.medicine101.ui.screens.noteeditview.components.BlockWrapper
// import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditHeaderBlock
// import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditTextBlock
// import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditListBlock
// import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditTableBlock
// import com.algorithmx.medicine101.ui.screens.noteeditview.components.EditDDBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditContent(
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    val blocks by viewModel.blocks.collectAsState()
    val title by viewModel.title.collectAsState()
    var showAddBlockSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { viewModel.updateTitle(it) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.saveNote() }) {
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
                        onDelete = { viewModel.deleteBlock(index) },
                        onMoveUp = { viewModel.moveBlock(index, index - 1) },
                        onMoveDown = { viewModel.moveBlock(index, index + 1) }
                    ) {
                        when (block.type) {
                            "header" -> EditHeaderBlock(block) { viewModel.updateBlock(index, it) }
                            "callout" -> EditTextBlock(block, "Callout Text") { viewModel.updateBlock(index, it) }
                            "list" -> EditListBlock(block) { viewModel.updateBlock(index, it) }
                            "table" -> EditTableBlock(block) { viewModel.updateBlock(index, it) }
                            "dd_table" -> EditDDBlock(block) { viewModel.updateBlock(index, it) }
                            else -> EditTextBlock(block, "Text Content") { viewModel.updateBlock(index, it) }
                        }
                    }
                }

                // Add Button at the bottom
                item {
                    Button(
                        // Make sure ContentBlock is imported correctly
                        onClick = { viewModel.addBlock(
                            ContentBlock(
                                type = "callout",
                                text = "New Block"
                            )
                        ) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 32.dp) // Added bottom padding so it isn't hidden by the FAB
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
                    viewModel.addBlock(newBlock)
                    showAddBlockSheet = false
                }
            )
        }
    }
}