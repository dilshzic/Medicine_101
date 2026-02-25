package com.algorithmx.medicine101.ui.screens.noteeditview.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock

@Composable
fun EditImageBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUpdate(block.copy(imageUrl = it.toString())) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Image Source", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Device")
            }
            
            OutlinedTextField(
                value = if (block.imageUrl?.startsWith("http") == true) block.imageUrl else "",
                onValueChange = { onUpdate(block.copy(imageUrl = it)) },
                label = { Text("Web URL") },
                modifier = Modifier.weight(1.5f),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
            )
        }
        
        if (block.imageUrl != null && !block.imageUrl.startsWith("http")) {
            Text(
                text = "Selected local image: ${block.imageUrl.takeLast(20)}...",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = block.text ?: "",
            onValueChange = { onUpdate(block.copy(text = it)) },
            label = { Text("Caption") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
