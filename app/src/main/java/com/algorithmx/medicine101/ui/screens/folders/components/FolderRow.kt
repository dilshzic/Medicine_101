package com.algorithmx.medicine101.ui.screens.folders.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.NoteEntity

@Composable
fun FolderRow(
    item: NoteEntity,
    onClick: () -> Unit, // This will now mean "navigate to contents"
    onPdfOpen: () -> Unit, // New callback for PDF navigation
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isPdfTocNode = item.pdfUri != null && item.pdfPage != null

    Card(
        onClick = if (isPdfTocNode) onPdfOpen else onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Icon Area - If it's a PDF TOC, clicking the icon goes DEEPER into TOC
            // Clicking the rest of the card (title) opens the PDF page.
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPdfTocNode) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        else Color(0xFFFFF8E1)
                    )
                    .clickable { onClick() }, // Always goes deeper
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPdfTocNode) Icons.Default.LibraryBooks else Icons.Default.Folder,
                    contentDescription = "Explore sub-sections",
                    tint = if (isPdfTocNode) MaterialTheme.colorScheme.secondary else Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
                
                // Small indicator that it has children
                Icon(
                    Icons.Default.ArrowDropDown,
                    null,
                    modifier = Modifier.align(Alignment.BottomEnd).size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPdfTocNode) "Section • Page ${item.pdfPage}" else "Folder",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isPdfTocNode) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = { showMenu = false; onRename() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Move") },
                        onClick = { showMenu = false; onMove() },
                        leadingIcon = { Icon(Icons.Default.DriveFileMove, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}
