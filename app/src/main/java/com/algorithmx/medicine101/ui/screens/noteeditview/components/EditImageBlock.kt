package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock

@Composable
fun EditImageBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Image Editor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        
        OutlinedTextField(
            value = block.imageUrl ?: "",
            onValueChange = { onUpdate(block.copy(imageUrl = it)) },
            label = { Text("Image File Name (e.g. diagram.webp)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        OutlinedTextField(
            value = block.text ?: "",
            onValueChange = { onUpdate(block.copy(text = it)) },
            label = { Text("Caption") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}