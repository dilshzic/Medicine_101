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
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.VideoTimestamp
import com.algorithmx.medicine101.utils.extractYoutubeId

@Composable
fun EditYouTubeBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    val timestamps = block.videoTimestamps ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text("YouTube Video Editor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Video ID Input
        OutlinedTextField(
            value = block.videoId ?: "",
            onValueChange = { rawInput ->
                // Clean the input immediately using your utility function
                val cleanVideoId = extractYoutubeId(rawInput)

                // FIX: Changed 'onBlockChange' to 'onUpdate' to match the parameter name
                onUpdate(block.copy(videoId = cleanVideoId))
            },
            label = { Text("YouTube Video ID or Link") },
            placeholder = { Text("Paste youtube.com/... or youtu.be/... link") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text(
            "Note: Use the ID from the URL (watch?v=THIS_PART)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        HorizontalDivider()

        // 2. Timestamps Editor
        Text("Timestamps Mapping", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))

        timestamps.forEachIndexed { index, ts ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    value = ts.timestamp,
                    onValueChange = {
                        val newList = timestamps.toMutableList()
                        newList[index] = ts.copy(timestamp = it)
                        onUpdate(block.copy(videoTimestamps = newList))
                    },
                    label = { Text("Time (MM:SS)") },
                    modifier = Modifier.weight(0.35f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = ts.label,
                    onValueChange = {
                        val newList = timestamps.toMutableList()
                        newList[index] = ts.copy(label = it)
                        onUpdate(block.copy(videoTimestamps = newList))
                    },
                    label = { Text("Description") },
                    modifier = Modifier.weight(0.65f),
                    singleLine = true
                )
                IconButton(onClick = {
                    val newList = timestamps.toMutableList().apply { removeAt(index) }
                    onUpdate(block.copy(videoTimestamps = newList))
                }) { Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error) }
            }
        }

        // Add Timestamp Button
        Button(
            onClick = {
                val newList = timestamps + VideoTimestamp(label = "New Topic", timestamp = "00:00")
                onUpdate(block.copy(videoTimestamps = newList))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Timestamp")
        }
    }
}