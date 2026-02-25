package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
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
import com.algorithmx.medicine101.data.FlowchartData
import com.algorithmx.medicine101.data.TabItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockCreationSheet(
    onDismiss: () -> Unit,
    onBlockSelected: (ContentBlock) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
                // Added vertical scroll in case the screen is small
                .verticalScroll(rememberScrollState())
        ) {
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

            // 4. Key-Value List
            BlockOption(Icons.Default.ListAlt, "Key-Value List") {
                onBlockSelected(ContentBlock(
                    type = "kv_list",
                    items = listOf(ContentItem(title = "Key", text = "Value"))
                ))
            }

            // 5. Table
            BlockOption(Icons.Default.TableChart, "Table") {
                onBlockSelected(ContentBlock(
                    type = "table",
                    tableHeaders = listOf("Col 1", "Col 2"),
                    tableRows = listOf(listOf("Data 1", "Data 2"))
                ))
            }

            // 6. Flowchart
            BlockOption(Icons.Default.AccountTree, "Flowchart") {
                onBlockSelected(ContentBlock(
                    type = "flowchart",
                    flowchart = FlowchartData(nodes = emptyList(), connections = emptyList())
                ))
            }

            // 7. Differential Diagnosis
            BlockOption(Icons.Default.MedicalServices, "Differential Diagnosis") {
                onBlockSelected(ContentBlock(
                    type = "dd_table",
                    ddItems = listOf(
                        DifferentialDiagnosis("Symptom", listOf("Disease A", "Disease B"))
                    )
                ))
            }

            // 8. Image
            BlockOption(Icons.Default.Image, "Image") {
                onBlockSelected(ContentBlock(
                    type = "image",
                    imageUrl = "" // Start empty, user will edit it
                ))
            }

            // 9. YouTube Video
            BlockOption(Icons.Default.PlayArrow, "YouTube Video") {
                onBlockSelected(ContentBlock(
                    type = "youtube",
                    videoId = "", // Start empty
                    videoTimestamps = emptyList()
                ))
            }

            // 10. Accordion / Tabs
            BlockOption(Icons.Default.ViewDay, "Accordion (Tabs)") {
                onBlockSelected(ContentBlock(
                    type = "accordion",
                    tabs = listOf(
                        TabItem(title = "Tab 1", content = emptyList())
                    )
                ))
            }

            // 11. Note Link (NEW)
            BlockOption(Icons.AutoMirrored.Filled.NoteAdd, "Link to Note") {
                onBlockSelected(ContentBlock(
                    type = "note_link",
                    linkedNoteId = "",
                    linkedNoteTitle = "Select a note..."
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
