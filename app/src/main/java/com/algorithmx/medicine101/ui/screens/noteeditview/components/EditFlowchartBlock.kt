package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock
import com.algorithmx.medicine101.data.FlowchartConnection
import com.algorithmx.medicine101.data.FlowchartData
import com.algorithmx.medicine101.data.FlowchartNode
import java.util.UUID

@Composable
fun EditFlowchartBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    // Ensure we have a valid FlowchartData object to work with
    val fcData = block.flowchart ?: FlowchartData("vertical", emptyList(), emptyList())
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text("Flowchart Editor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)

        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(vertical = 8.dp)) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Nodes (Boxes)") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Connections (Arrows)") })
        }

        if (selectedTab == 0) {
            // --- EDIT NODES ---
            fcData.nodes.forEachIndexed { index, node ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = node.label,
                                onValueChange = { newLabel ->
                                    val newNodes = fcData.nodes.toMutableList()
                                    newNodes[index] = node.copy(label = newLabel)
                                    onUpdate(block.copy(flowchart = fcData.copy(nodes = newNodes)))
                                },
                                label = { Text("Label (ID: ${node.id})") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val newNodes = fcData.nodes.toMutableList().apply { removeAt(index) }
                                onUpdate(block.copy(flowchart = fcData.copy(nodes = newNodes)))
                            }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                        }
                        
                        // Level and Order inputs
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            OutlinedTextField(
                                value = node.level.toString(),
                                onValueChange = { 
                                    val newNodes = fcData.nodes.toMutableList()
                                    newNodes[index] = node.copy(level = it.toIntOrNull() ?: 0)
                                    onUpdate(block.copy(flowchart = fcData.copy(nodes = newNodes)))
                                },
                                label = { Text("Row (Level)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = node.order.toString(),
                                onValueChange = { 
                                    val newNodes = fcData.nodes.toMutableList()
                                    newNodes[index] = node.copy(order = it.toIntOrNull() ?: 0)
                                    onUpdate(block.copy(flowchart = fcData.copy(nodes = newNodes)))
                                },
                                label = { Text("Col (Order)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Button(onClick = {
                val newNode = FlowchartNode(
                    id = UUID.randomUUID().toString().take(4), // Short random ID
                    label = "New Step",
                    level = (fcData.nodes.maxOfOrNull { it.level } ?: -1) + 1,
                    order = 0
                )
                onUpdate(block.copy(flowchart = fcData.copy(nodes = fcData.nodes + newNode)))
            }, modifier = Modifier.fillMaxWidth()) { Text("Add Node") }

        } else {
            // --- EDIT CONNECTIONS ---
            fcData.connections.forEachIndexed { index, conn ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    OutlinedTextField(
                        value = conn.from,
                        onValueChange = { 
                            val newConns = fcData.connections.toMutableList()
                            newConns[index] = conn.copy(from = it)
                            onUpdate(block.copy(flowchart = fcData.copy(connections = newConns)))
                        },
                        label = { Text("From ID") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        value = conn.to,
                        onValueChange = { 
                            val newConns = fcData.connections.toMutableList()
                            newConns[index] = conn.copy(to = it)
                            onUpdate(block.copy(flowchart = fcData.copy(connections = newConns)))
                        },
                        label = { Text("To ID") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        value = conn.label ?: "",
                        onValueChange = { 
                            val newConns = fcData.connections.toMutableList()
                            newConns[index] = conn.copy(label = it.ifBlank { null })
                            onUpdate(block.copy(flowchart = fcData.copy(connections = newConns)))
                        },
                        label = { Text("Arrow Text") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val newConns = fcData.connections.toMutableList().apply { removeAt(index) }
                        onUpdate(block.copy(flowchart = fcData.copy(connections = newConns)))
                    }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                }
            }

            Button(onClick = {
                val newConn = FlowchartConnection(from = "", to = "", label = null)
                onUpdate(block.copy(flowchart = fcData.copy(connections = fcData.connections + newConn)))
            }, modifier = Modifier.fillMaxWidth()) { Text("Add Connection") }
        }
    }
}