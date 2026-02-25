package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.FlowchartData

@Composable
fun EditFlowchartBlock(
    block: ContentBlock,
    onEditChart: () -> Unit,
    onUpdate: (ContentBlock) -> Unit
) {
    val fcData = block.flowchart ?: FlowchartData(emptyList(), emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Flowchart: ${fcData.nodes.size} nodes, ${fcData.connections.size} connections",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Button(
                onClick = onEditChart,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Edit Canvas", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (fcData.nodes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hand-drawn logic content exists. Use the canvas to modify geometry and text.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onEditChart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Drawing Flowchart")
            }
        }
        
        IconButton(
            onClick = { onUpdate(block.copy(flowchart = null)) },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Delete, "Clear", tint = MaterialTheme.colorScheme.error)
        }
    }
}
