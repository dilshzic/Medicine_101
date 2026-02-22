package com.algorithmx.medicine101.ui.screens.noteview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
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
        // Add a subtle line under H1 headers only
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
    // The error "ambiguous component1" happens because Icons wasn't imported,
    // so the compiler didn't know this was a Triple<Color, Color, ImageVector>
    val (containerColor, contentColor, icon) = when (variant.lowercase()) {
        "warning", "alert" -> Triple(
            Color(0xFFFFF3E0), // Light Orange
            Color(0xFFE65100), // Dark Orange
            Icons.Default.Warning
        )
        "error", "danger" -> Triple(
            Color(0xFFFFEBEE), // Light Red
            Color(0xFFB71C1C), // Dark Red
            Icons.Default.Error
        )
        else -> Triple(
            MaterialTheme.colorScheme.primaryContainer, // Light Teal
            MaterialTheme.colorScheme.primary,          // Dark Teal
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
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
    ) {
        // Render Header Row
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
                    fontSize = 14.sp
                )
            }
        }

        // Render Data Rows
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .background(if (index % 2 == 0) Color.Transparent else Color(0xFFF5F5F5))
            ) {
                row.forEach { cell ->
                    Text(text = cell, modifier = Modifier.weight(1f), fontSize = 14.sp)
                }
            }
            // Fixed: Divider -> HorizontalDivider
            if (index < rows.size - 1) {
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            }
        }
    }
}

// 4. BULLET LIST BLOCK
@Composable
fun BulletListBlock(items: List<ContentItem>, depth: Int = 0) { // Changed ListItem to ContentItem
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        items.forEach { item ->
            Row(modifier = Modifier.padding(start = (depth * 16).dp, bottom = 4.dp)) {
                Text(
                    text = "•",
                    modifier = Modifier.padding(end = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                Column {
                    // Assuming ContentItem has 'text' property.
                    // If ContentItem uses 'text?' (nullable), use 'text ?: ""'
                    Text(text = item.text ?: "", style = MaterialTheme.typography.bodyLarge)

                    // RECURSION
                    // Assuming ContentItem has 'content' or 'subItems' property
                    // Based on previous prompts, ContentItem usually has 'content' (List<ContentBlock>)
                    // or specific sub-items. Adjust property name if needed.
                    /* If ContentItem structure is recursive list items:
                       item.subItems?.let { subs -> BulletListBlock(items = subs, depth = depth + 1) }
                    */
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
            model = "file:///android_asset/$url",
            contentDescription = caption ?: "Medical Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        if (!caption.isNullOrEmpty()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun TabGroupBlock(
    tabs: List<TabItem>
    // REMOVED: onRenderContent: @Composable (ContentBlock) -> Unit
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

            // We render the blocks directly here using the imported renderer
            currentBlocks.forEach { block ->
                RenderSingleBlock(block)
            }
        }
    }
}