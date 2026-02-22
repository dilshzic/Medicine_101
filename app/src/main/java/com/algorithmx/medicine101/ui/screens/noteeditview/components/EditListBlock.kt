package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.ContentItem

@Composable
fun EditListBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    val items = block.items ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("Bullet List", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)

        // Call the recursive tree editor
        NestedListEditor(
            items = items,
            depth = 0,
            onItemsChange = { newItems ->
                onUpdate(block.copy(items = newItems))
            }
        )
    }
}

// --- NEW: RECURSIVE LIST EDITOR ---
@Composable
fun NestedListEditor(
    items: List<ContentItem>,
    depth: Int,
    onItemsChange: (List<ContentItem>) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(vertical = 4.dp).padding(start = (depth * 16).dp)
            ) {
                Text(
                    text = if (depth == 0) "•" else "◦",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 8.dp, top = 8.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = item.text ?: "",
                            onValueChange = { newText ->
                                val newItems = items.toMutableList()
                                newItems[index] = item.copy(text = newText)
                                onItemsChange(newItems)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = false
                        )

                        // Delete this specific item
                        IconButton(onClick = {
                            val newItems = items.toMutableList()
                            newItems.removeAt(index)
                            onItemsChange(newItems)
                        }) {
                            Icon(Icons.Default.Close, "Remove Item", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Button to add a sub-item TO THIS ITEM
                    TextButton(
                        onClick = {
                            val currentSubs = item.subItems ?: emptyList()
                            val newSubs = currentSubs + ContentItem(text = "")
                            val newItems = items.toMutableList()
                            newItems[index] = item.copy(subItems = newSubs)
                            onItemsChange(newItems)
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("+ Sub-item", fontSize = 12.sp)
                    }

                    // Recursively render existing sub-items
                    if (!item.subItems.isNullOrEmpty()) {
                        NestedListEditor(
                            items = item.subItems,
                            depth = depth + 1,
                            onItemsChange = { updatedSubItems ->
                                val newItems = items.toMutableList()
                                newItems[index] = item.copy(subItems = updatedSubItems)
                                onItemsChange(newItems)
                            }
                        )
                    }
                }
            }
        }

        // Add Item Button for this specific depth level
        TextButton(
            onClick = {
                val newItems = items + ContentItem(text = "")
                onItemsChange(newItems)
            },
            modifier = Modifier.padding(start = (depth * 16).dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (depth == 0) "Add Main Bullet" else "Add Sub Bullet")
        }
    }
}