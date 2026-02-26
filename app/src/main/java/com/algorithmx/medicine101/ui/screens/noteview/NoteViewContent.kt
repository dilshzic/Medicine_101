package com.algorithmx.medicine101.ui.screens.noteview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.ui.screens.NoteViewModel
import com.algorithmx.medicine101.ui.screens.noteview.components.UniversalRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewContent(
    viewModel: NoteViewModel,
    onNoteLinkClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val blocks by viewModel.blocks.collectAsState()
    val title by viewModel.title.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val availableTabs = blocks.map { it.tabName ?: "General" }.distinct().ifEmpty { listOf("General") }
    val currentTabBlocks = blocks.filter { (it.tabName ?: "General") == selectedTab }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { 
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SecondaryScrollableTabRow(
                selectedTabIndex = availableTabs.indexOf(selectedTab).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {} // Clean look
            ) {
                availableTabs.forEach { tabName ->
                    Tab(
                        selected = tabName == selectedTab,
                        onClick = { viewModel.selectTab(tabName) },
                        text = { 
                            Text(
                                text = tabName,
                                style = if (tabName == selectedTab) 
                                    MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                else 
                                    MaterialTheme.typography.titleSmall
                            ) 
                        }
                    )
                }
            }

            UniversalRenderer(
                blocks = currentTabBlocks,
                onNoteLinkClick = onNoteLinkClick
            )
        }
    }
}
