package com.algorithmx.medicine101.ui.screens.noteview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.ui.screens.NoteViewModel
import com.algorithmx.medicine101.ui.screens.noteview.components.UniversalRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewContent(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val blocks by viewModel.blocks.collectAsState()
    val title by viewModel.title.collectAsState()

    // 1. Collect the selected tab state from the ViewModel
    val selectedTab by viewModel.selectedTab.collectAsState()

    // 2. Generate the list of available tabs from the blocks
    val availableTabs = blocks.map { it.tabName }.distinct().ifEmpty { listOf("General") }

    // 3. Filter blocks so we ONLY render the ones belonging to the active tab
    val currentTabBlocks = blocks.filter { it.tabName == selectedTab }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = { /* Back button */ },
                actions = {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // --- NEW: TOP TAB ROW FOR READ MODE ---
            ScrollableTabRow(
                selectedTabIndex = availableTabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                availableTabs.forEach { tabName ->
                    Tab(
                        selected = tabName == selectedTab,
                        onClick = { viewModel.selectTab(tabName) }, // Switch tabs
                        text = { Text(tabName) }
                    )
                }
            }

            // --- UPDATED: Pass the filtered blocks, not the whole list ---
            UniversalRenderer(blocks = currentTabBlocks)
        }
    }
}