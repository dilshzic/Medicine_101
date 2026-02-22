package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock

@Composable
fun EditHeaderBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = block.text ?: "",
            onValueChange = { onUpdate(block.copy(text = it)) },
            label = { Text("Header Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // H1 / H2 Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Level: ", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = block.level == 1,
                onClick = { onUpdate(block.copy(level = 1)) },
                label = { Text("H1 (Main)") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = block.level == 2,
                onClick = { onUpdate(block.copy(level = 2)) },
                label = { Text("H2 (Sub)") }
            )
        }
    }
}

@Composable
fun EditCalloutBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    val currentVariant = block.variant ?: "info"

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = block.text ?: "",
            onValueChange = { onUpdate(block.copy(text = it)) },
            label = { Text("Callout Text") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        // Variant Selector (Color/Icon)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Type: ", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = currentVariant == "info",
                onClick = { onUpdate(block.copy(variant = "info")) },
                label = { Text("Info (Blue)") }
            )
            Spacer(modifier = Modifier.width(4.dp))
            FilterChip(
                selected = currentVariant == "warning",
                onClick = { onUpdate(block.copy(variant = "warning")) },
                label = { Text("Warning (Orange)") }
            )
            Spacer(modifier = Modifier.width(4.dp))
            FilterChip(
                selected = currentVariant == "error",
                onClick = { onUpdate(block.copy(variant = "error")) },
                label = { Text("Error (Red)") }
            )
        }
    }
}

@Composable
fun EditTextBlock(
    block: ContentBlock,
    label: String = "Content",
    onUpdate: (ContentBlock) -> Unit
) {
    OutlinedTextField(
        value = block.text ?: "",
        onValueChange = { onUpdate(block.copy(text = it)) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
}