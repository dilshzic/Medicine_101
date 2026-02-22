package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.ContentItem

@Composable
fun EditAccordionBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    val items = block.items ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text("Accordion Editor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

        items.forEachIndexed { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // --- 1. Edit Section Title ---
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = item.title ?: "",
                            onValueChange = { newTitle ->
                                val newItems = items.toMutableList()
                                newItems[index] = item.copy(title = newTitle)
                                onUpdate(block.copy(items = newItems))
                            },
                            label = { Text("Section Title") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            val newItems = items.toMutableList().apply { removeAt(index) }
                            onUpdate(block.copy(items = newItems))
                        }) {
                            Icon(Icons.Default.Delete, "Delete Section", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    // --- 2. Edit Inner Blocks (Simplified to Text) ---
                    val innerBlocks = item.content ?: emptyList()
                    innerBlocks.forEachIndexed { innerIndex, innerBlock ->
                        OutlinedTextField(
                            value = innerBlock.text ?: "",
                            onValueChange = { newText ->
                                val newInnerBlocks = innerBlocks.toMutableList()
                                newInnerBlocks[innerIndex] = innerBlock.copy(text = newText)
                                
                                val newItems = items.toMutableList()
                                newItems[index] = item.copy(content = newInnerBlocks)
                                onUpdate(block.copy(items = newItems))
                            },
                            label = { Text("Inner Content (${innerBlock.type})") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 16.dp),
                            minLines = 2
                        )
                    }

                    // Add Inner Content Button
                    TextButton(onClick = {
                        val newInnerBlocks = innerBlocks + ContentBlock(type = "text", text = "")
                        val newItems = items.toMutableList()
                        newItems[index] = item.copy(content = newInnerBlocks)
                        onUpdate(block.copy(items = newItems))
                    }, modifier = Modifier.padding(start = 16.dp)) {
                        Text("+ Add inner text block")
                    }
                }
            }
        }

        // Add Section Button
        Button(
            onClick = {
                val newItems = items + ContentItem(title = "New Section", content = emptyList())
                onUpdate(block.copy(items = newItems))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Accordion Section")
        }
    }
}