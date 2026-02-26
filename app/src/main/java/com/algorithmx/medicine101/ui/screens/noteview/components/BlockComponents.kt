package com.algorithmx.medicine101.ui.screens.noteview.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.algorithmx.medicine101.data.ContentItem
import com.algorithmx.medicine101.data.TabItem

// 1. HEADER BLOCK
@Composable
fun HeaderBlock(text: String, level: Int) {
    val style = if (level == 1) {
        MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    } else {
        MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Column(modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)) {
        Text(text = text, style = style)
        if (level == 1) {
            HorizontalDivider(
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.padding(top = 4.dp, end = 100.dp)
            )
        }
    }
}

// 2. CALLOUT BLOCK
@Composable
fun CalloutBlock(text: String, variant: String = "info") {
    val isDark = isSystemInDarkTheme()
    val (containerColor, contentColor, icon) = when (variant.lowercase()) {
        "warning", "alert" -> Triple(
            if (isDark) Color(0xFF3E2723) else Color(0xFFFFF3E0),
            if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100),
            Icons.Default.Warning
        )
        "error", "danger" -> Triple(
            if (isDark) Color(0xFF311B92) else Color(0xFFFFEBEE),
            if (isDark) Color(0xFFEF5350) else Color(0xFFB71C1C),
            Icons.Default.Error
        )
        else -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            Icons.Default.Info
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(containerColor, RoundedCornerShape(8.dp))
            .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// 3. TABLE BLOCK
@Composable
fun TableBlock(headers: List<String>, rows: List<List<String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            headers.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        rows.forEachIndexed { index, row ->
            val isAlternate = index % 2 != 0
            val rowBackground = if (isAlternate) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            } else {
                Color.Transparent
            }

            Row(
                modifier = Modifier
                    .background(rowBackground)
                    .padding(8.dp)
            ) {
                row.forEach { cell ->
                    Text(
                        text = cell, 
                        modifier = Modifier.weight(1f), 
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (index < rows.size - 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            }
        }
    }
}

// 4. BULLET LIST BLOCK
@Composable
fun BulletListBlock(items: List<ContentItem>, depth: Int = 0) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        items.forEach { item ->
            Row(modifier = Modifier.padding(start = (depth * 16).dp, bottom = 4.dp)) {
                Text(
                    text = if (depth == 0) "•" else "◦",
                    modifier = Modifier.padding(end = 8.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Column {
                    if (!item.text.isNullOrEmpty()) {
                        Text(
                            text = item.text, 
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    item.subItems?.let { subs ->
                        BulletListBlock(items = subs, depth = depth + 1)
                    }
                }
            }
        }
    }
}

// 5. IMAGE BLOCK
@Composable
fun ImageBlock(url: String, caption: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = url,
            contentDescription = caption ?: "Medical Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )

        if (!caption.isNullOrEmpty()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// 6. NOTE LINK BLOCK
@Composable
fun NoteLinkBlock(noteId: String, noteTitle: String, onClick: (String) -> Unit) {
    Card(
        onClick = { onClick(noteId) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "See also:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = noteTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun TabGroupBlock(
    tabs: List<TabItem>
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Column(modifier = Modifier.padding(top = 16.dp)) {
            val currentBlocks = tabs[selectedTabIndex].content ?: emptyList()
            currentBlocks.forEach { block ->
                RenderSingleBlock(block)
            }
        }
    }
}


// 7. ACCORDION BLOCK
@Composable
fun AccordionBlock(items: List<ContentItem>) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        items.forEach { item ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                onClick = { expanded = !expanded }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.title ?: "Section",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                            item.content?.forEach { innerBlock ->
                                RenderSingleBlock(innerBlock)
                            }
                        }
                    }
                }
            }
        }
    }
}
