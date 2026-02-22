package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.ContentItem


@Composable
fun EditListBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    val items = block.items ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Bullet List", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        
        items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("•", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(end = 8.dp))
                
                OutlinedTextField(
                    value = item.text ?: "",
                    onValueChange = { newText ->
                        // Update specific item text
                        val newItems = items.toMutableList()
                        newItems[index] = item.copy(text = newText)
                        onUpdate(block.copy(items = newItems))
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = false
                )

                IconButton(onClick = {
                    // Remove item
                    val newItems = items.toMutableList()
                    newItems.removeAt(index)
                    onUpdate(block.copy(items = newItems))
                }) {
                    Icon(Icons.Default.Close, "Remove Item", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Add Item Button
        TextButton(
            onClick = {
                val newItems = items + ContentItem(text = "")
                onUpdate(block.copy(items = newItems))
            }
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Bullet Point")
        }
    }
}