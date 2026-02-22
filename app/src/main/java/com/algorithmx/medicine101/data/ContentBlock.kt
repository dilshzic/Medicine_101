package com.algorithmx.medicine101.data

// This single class handles Headers, Lists, Tables, and Warnings
data class ContentBlock(
    val type: String,               // "header", "list", "table", "callout", "kv_list"
    val text: String? = null,       // Used for headers, warnings, or simple text
    val level: Int = 1,             // For headers (1=Big, 2=Small)
    val items: List<ContentItem>? = null, // For bullet lists
    val tableHeaders: List<String>? = null, // For table columns
    val tableRows: List<List<String>>? = null, // For table data
    val imageUrl: String? = null, // For images
    val tabs: List<TabItem>? = null,
    val flowchart: FlowchartData? = null,
    val ddItems: List<DifferentialDiagnosis>? = null,

    // FIX 1: Change 'List<ListItem>?' to 'List<ContentItem>?'

    // FIX 2: Add this field
    val variant: String? = null,
)

// Helper class for nested bullet points
data class ListItem(
    val text: String,
    val subItems: List<ListItem>? = null // Universal recursion: list inside a list
)

// Helper for "Key-Value" definitions (e.g., Cachexia: Weight loss)
data class KeyValueItem(
    val key: String,
    val value: String
)
data class TabItem(
    val title: String,
    val content: List<ContentBlock> // Each tab has its own list of blocks!
)
data class FlowchartData(
    val direction: String = "vertical", // "vertical" or "horizontal"
    val nodes: List<FlowchartNode>,
    val connections: List<FlowchartConnection>
)

data class FlowchartNode(
    val id: String,
    val label: String,
    val type: String = "process", // "start", "decision", "process", "end"
    val level: Int,               // Vertical rank (0 = top, 1 = next row down...)
    val order: Int                // Horizontal order (0 = left, 1 = right...)
)

data class FlowchartConnection(
    val from: String,
    val to: String,
    val label: String? = null     // Text on the arrow (e.g., "Yes", "No")
)

data class DifferentialDiagnosis(
    val finding: String,         // e.g. "Chest Pain"
    val diagnoses: List<String>, // e.g. ["MI", "GERD", "Pneumonia"]
    val likelihood: String? = null // Optional: "High", "Medium", "Red Flag"
)
// ... existing ContentBlock class ...

data class ContentItem(
    val id: String = java.util.UUID.randomUUID().toString(), // Unique ID for Compose keys
    val text: String? = null,        // For Bullet Points
    val title: String? = null,       // For Accordion Headers
    val content: List<ContentBlock>? = null // For Nested Accordion Content
)