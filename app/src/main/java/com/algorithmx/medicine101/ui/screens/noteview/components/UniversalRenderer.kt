package com.algorithmx.medicine101.ui.screens.noteview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
        items(
            items = blocks,
            key = { block ->
                // Generate a unique ID based on content hash to help Compose
                (block.text.hashCode() + block.type.hashCode() + (block.items?.size ?: 0)).toString()
            }
        ) { block ->
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

        "table" -> {
            if (block.tableHeaders != null && block.tableRows != null) {
                TableBlock(headers = block.tableHeaders, rows = block.tableRows)
            }
        }

        "image" -> block.imageUrl?.let {
            ImageBlock(url = it, caption = block.text)
        }

        // --- FIX IS HERE ---
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