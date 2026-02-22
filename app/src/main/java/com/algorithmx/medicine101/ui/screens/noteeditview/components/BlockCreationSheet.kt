package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.ContentItem
import com.algorithmx.medicine101.data.DifferentialDiagnosis


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockCreationSheet(
    onDismiss: () -> Unit,
    onBlockSelected: (ContentBlock) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text(
                "Insert Block",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 1. Header
            BlockOption(Icons.Default.Title, "Header") {
                onBlockSelected(ContentBlock(type = "header", text = "New Header", level = 1))
            }

            // 2. Paragraph (Callout)
            BlockOption(Icons.Default.ShortText, "Paragraph") {
                onBlockSelected(ContentBlock(type = "callout", text = "", variant = "info"))
            }

            // 3. Bullet List
            BlockOption(Icons.Default.FormatListBulleted, "Bullet List") {
                onBlockSelected(ContentBlock(type = "list", items = listOf(ContentItem(text = "Item 1"))))
            }

            // 4. Table
            BlockOption(Icons.Default.TableChart, "Table") {
                onBlockSelected(ContentBlock(
                    type = "table",
                    tableHeaders = listOf("Col 1", "Col 2"),
                    tableRows = listOf(listOf("Data 1", "Data 2"))
                ))
            }
            
            // 5. Flowchart (Placeholder)
            BlockOption(Icons.Default.AccountTree, "Flowchart (Basic)") {
                 // For now, we insert a placeholder or basic flowchart structure
                 // You can expand this once we build the Flowchart Editor
                 onBlockSelected(ContentBlock(type = "callout", text = "[Flowchart Placeholder]"))
            }
            // 6. Differential Diagnosis
            BlockOption(Icons.Default.MedicalServices, "Differential Diagnosis") {
                onBlockSelected(ContentBlock(
                    type = "dd_table", // New Type ID
                    ddItems = listOf(
                        DifferentialDiagnosis("Symptom", listOf("Disease A", "Disease B"))
                    )
                ))
            }
        }
    }
}

@Composable
fun BlockOption(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}