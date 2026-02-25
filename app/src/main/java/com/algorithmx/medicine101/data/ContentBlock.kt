package com.algorithmx.medicine101.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class NodeType {
    RECTANGLE, // For generic process steps
    DIAMOND,   // For Decisions (Yes/No)
    OVAL,      // For Start/End points
    UNKNOWN    // Fallback
}

@Serializable
data class FlowNode(
    val id: String = UUID.randomUUID().toString(),
    val type: NodeType = NodeType.UNKNOWN,
    val text: String = "",
    val centerX: Float,
    val centerY: Float,
    val width: Float = 200f,  // Default width
    val height: Float = 100f  // Default height
)

@Serializable
data class FlowConnection(
    val id: String = UUID.randomUUID().toString(),
    val fromNodeId: String,
    val toNodeId: String,
    val label: String? = null // e.g., "Yes", "No"
)

@Serializable
data class FlowchartMetadata(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val lastModified: Long = System.currentTimeMillis()
)

@Serializable
data class FlowchartData(
    val nodes: List<FlowNode>,
    val connections: List<FlowConnection>
)

@Serializable
data class FlowchartFile(
    val id: String,
    val title: String,
    val nodes: List<FlowNode>,
    val connections: List<FlowConnection>,
    val lastModified: Long = System.currentTimeMillis()
)

@Serializable
data class ContentBlock(
    val type: String,               // "header", "list", "table", "callout", "kv_list", "note_link", "flowchart"
    val text: String? = null,       // Used for headers, warnings, or simple text
    val level: Int = 1,             // For headers (1=Big, 2=Small)
    val items: List<ContentItem>? = null, // For bullet lists
    val tableHeaders: List<String>? = null, // For table columns
    val tableRows: List<List<String>>? = null, // For table data
    val imageUrl: String? = null, // For images
    val tabs: List<TabItem>? = null,
    val flowchart: FlowchartData? = null,
    val ddItems: List<DifferentialDiagnosis>? = null,
    val tabName: String = "General",

    val videoId: String? = null,
    val videoTimestamps: List<VideoTimestamp>? = null,

    val variant: String? = null,
    
    // AI Integration
    val aiInstructions: String? = null,

    // Link to other notes
    val linkedNoteId: String? = null,
    val linkedNoteTitle: String? = null
)

@Serializable
data class ListItem(
    val text: String,
    val subItems: List<ListItem>? = null 
)

@Serializable
data class KeyValueItem(
    val key: String,
    val value: String
)

@Serializable
data class TabItem(
    val title: String,
    val content: List<ContentBlock> 
)

@Serializable
data class DifferentialDiagnosis(
    val finding: String,         
    val diagnoses: List<String>, 
    val likelihood: String? = null 
)

@Serializable
data class ContentItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String? = null,
    val title: String? = null,
    val content: List<ContentBlock>? = null,
    val subItems: List<ContentItem>? = null
)

@Serializable
data class VideoTimestamp(
    val label: String,
    val timestamp: String
) {
    fun toSeconds(): Float {
        return try {
            val parts = timestamp.split(":")
            when (parts.size) {
                2 -> parts[0].toFloat() * 60 + parts[1].toFloat()
                3 -> parts[0].toFloat() * 3600 + parts[1].toFloat() * 60 + parts[2].toFloat()
                else -> timestamp.toFloat()
            }
        } catch (e: Exception) {
            0f
        }
    }
}
