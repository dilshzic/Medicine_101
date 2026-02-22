package com.algorithmx.medicine101.ui.screens.noteview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // <-- NEW IMPORT
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.ContentBlock

@Composable
fun UniversalRenderer(blocks: List<ContentBlock>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- FIX: Use itemsIndexed to get the list index ---
        itemsIndexed(
            items = blocks,
            key = { index, block ->
                // By prefixing the list index, every key is guaranteed to be 100% unique
                // even if two blocks contain the exact same text/type.
                "${index}_${block.type}_${block.text?.hashCode()}"
            }
        ) { _, block ->
            RenderSingleBlock(block)
        }
    }
}

@Composable
fun RenderSingleBlock(block: ContentBlock) {
    when (block.type) {
        "header" -> HeaderBlock(
            text = block.text ?: "",
            level = block.level
        )

        "callout" -> CalloutBlock(
            text = block.text ?: "",
            // Pass the variant (e.g., "warning", "info")
            variant = block.variant ?: "info"
        )

        "list" -> {
            block.items?.let {
                BulletListBlock(items = it)
            }
        }
        "accordion" -> {
            block.items?.let {
                AccordionBlock(items = it)
            }
        }

        "table" -> {
            if (block.tableHeaders != null && block.tableRows != null) {
                TableBlock(headers = block.tableHeaders, rows = block.tableRows)
            }
        }

        "image" -> block.imageUrl?.let {
            ImageBlock(url = it, caption = block.text)
        }

        "tab_group", "tabs" -> {
            block.tabs?.let {
                // We ONLY pass the tabs list.
                // TabGroupBlock in BlockComponents.kt now calls RenderSingleBlock internally.
                TabGroupBlock(tabs = it)
            }
        }

        "flowchart" -> {
            block.flowchart?.let {
                FlowchartRenderer(data = it)
            }
        }

        "dd_table" -> {
            block.ddItems?.let {
                DifferentialDiagnosisBlock(items = it)
            }
        }
// ... inside RenderSingleBlock ...
        "youtube" -> {
            if (!block.videoId.isNullOrEmpty()) {
                YouTubeBlock(videoId = block.videoId, timestamps = block.videoTimestamps)
            }
        }

        else -> {
            if (!block.text.isNullOrEmpty()) {
                Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}