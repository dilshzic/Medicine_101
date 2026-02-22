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
import com.algorithmx.medicine101.data.DifferentialDiagnosis

@Composable
fun EditDDBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    val items = block.ddItems ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            "Differential Diagnosis Editor",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        items.forEachIndexed { index, item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Row 1: Delete Button & Finding Input
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = item.finding,
                            onValueChange = { newFinding ->
                                val newItems = items.toMutableList()
                                newItems[index] = item.copy(finding = newFinding)
                                onUpdate(block.copy(ddItems = newItems))
                            },
                            label = { Text("Finding (e.g. Chest Pain)") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            val newItems = items.toMutableList()
                            newItems.removeAt(index)
                            onUpdate(block.copy(ddItems = newItems))
                        }) {
                            Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row 2: Diagnoses Input
                    OutlinedTextField(
                        value = item.diagnoses.joinToString(", "),
                        onValueChange = { input ->
                            // Convert comma-separated string back to List
                            val newDiagnoses = input.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val newItems = items.toMutableList()
                            newItems[index] = item.copy(diagnoses = newDiagnoses)
                            onUpdate(block.copy(ddItems = newItems))
                        },
                        label = { Text("Diagnoses (comma separated)") },
                        placeholder = { Text("MI, GERD, PE...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Row 3: Likelihood Input
                    OutlinedTextField(
                        value = item.likelihood ?: "",
                        onValueChange = { newLikelihood ->
                            val newItems = items.toMutableList()
                            newItems[index] = item.copy(likelihood = newLikelihood.ifBlank { null })
                            onUpdate(block.copy(ddItems = newItems))
                        },
                        label = { Text("Likelihood (Optional)") },
                        placeholder = { Text("e.g. High, Medium, Red Flag") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Add Button
        Button(
            onClick = {
                val newItems = items + DifferentialDiagnosis(finding = "", diagnoses = emptyList())
                onUpdate(block.copy(ddItems = newItems))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Finding")
        }
    }
}