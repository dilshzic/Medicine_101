package com.algorithmx.medicine101.ui.screens.brain

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.algorithmx.medicine101.brain.BrainCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainManagerScreen(
    viewModel: BrainManagerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isModelExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Brain Manager") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { viewModel.saveConfiguration { onNavigateBack() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Configuration")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Stats Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BrainStatItem("Requests", uiState.totalRequests.toString(), Icons.Default.ChatBubble)
                    VerticalDivider(modifier = Modifier.height(40.dp))
                    BrainStatItem("Est. Tokens", uiState.totalTokensUsed.toString(), Icons.Default.Bolt)
                }
            }

            Text("Model Selection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Category Selection
            ExposedDropdownMenuBox(
                expanded = isCategoryExpanded,
                onExpandedChange = { isCategoryExpanded = !isCategoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.selectedCategory.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Capability Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = MaterialTheme.shapes.medium,
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = isCategoryExpanded,
                    onDismissRequest = { isCategoryExpanded = false }
                ) {
                    BrainCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.replace("_", " ")) },
                            onClick = {
                                viewModel.updateCategory(category)
                                isCategoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Model Selection (Filtered by Category)
            ExposedDropdownMenuBox(
                expanded = isModelExpanded,
                onExpandedChange = { isModelExpanded = !isModelExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("AI Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = MaterialTheme.shapes.medium,
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = isModelExpanded,
                    onDismissRequest = { isModelExpanded = false }
                ) {
                    uiState.availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                viewModel.updateModel(model)
                                isModelExpanded = false
                            }
                        )
                    }
                }
            }

            // Provider Info (Read-only)
            OutlinedTextField(
                value = uiState.selectedProvider.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Automatic Provider") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )

            // System Instruction
            OutlinedTextField(
                value = uiState.systemInstruction,
                onValueChange = { viewModel.updateSystemInstruction(it) },
                label = { Text("System Instructions (Behavior)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text("e.g. You are a medical professional helping with diagnosis...") }
            )
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Models are categorized by clinical capability. The app automatically selects the best API gateway based on your choice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun BrainStatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}
