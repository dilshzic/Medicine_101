package com.algorithmx.medmate.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithmx.medicine101.data.ContentBlock

@Composable
fun EditTableBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    val headers = block.tableHeaders ?: emptyList()
    val rows = block.tableRows ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Table Editor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

            // --- NEW: COLUMN CONTROLS ---
            Row {
                TextButton(onClick = {
                    val newHeaders = headers + "New Col"
                    val newRows = rows.map { it + "" }
                    onUpdate(block.copy(tableHeaders = newHeaders, tableRows = newRows))
                }) { Text("+ Col") }

                TextButton(
                    onClick = {
                        if (headers.size > 1) {
                            val newHeaders = headers.dropLast(1)
                            val newRows = rows.map { it.dropLast(1) }
                            onUpdate(block.copy(tableHeaders = newHeaders, tableRows = newRows))
                        }
                    },
                    enabled = headers.size > 1
                ) { Text("- Col", color = if (headers.size > 1) MaterialTheme.colorScheme.error else Color.Gray) }
            }
        }

        // --- 1. HEADERS ---
        Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
            headers.forEachIndexed { index, title ->
                TableCellEditor(
                    value = title,
                    isHeader = true,
                    onValueChange = { newText ->
                        val newHeaders = headers.toMutableList()
                        newHeaders[index] = newText
                        onUpdate(block.copy(tableHeaders = newHeaders))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
        }

        HorizontalDivider()

        // --- 2. ROWS ---
        rows.forEachIndexed { rowIndex, row ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                row.forEachIndexed { colIndex, cellValue ->
                    TableCellEditor(
                        value = cellValue,
                        isHeader = false,
                        onValueChange = { newText ->
                            val newRows = rows.map { it.toMutableList() }.toMutableList()
                            newRows[rowIndex][colIndex] = newText
                            onUpdate(block.copy(tableRows = newRows))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                IconButton(
                    onClick = {
                        val newRows = rows.toMutableList()
                        newRows.removeAt(rowIndex)
                        onUpdate(block.copy(tableRows = newRows))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete Row", tint = MaterialTheme.colorScheme.error)
                }
            }
            HorizontalDivider()
        }

        // --- 3. ADD ROW BUTTON ---
        Button(
            onClick = {
                val blankRow = List(headers.size) { "" }
                val newRows = rows + listOf(blankRow)
                onUpdate(block.copy(tableRows = newRows))
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Row")
        }
    }
}

@Composable
fun TableCellEditor(
    value: String,
    isHeader: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )
        if (value.isEmpty()) {
            Text(text = if(isHeader) "Header" else "...", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}