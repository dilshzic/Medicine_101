package com.algorithmx.medicine101.data.models

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
    val width: Float = 200f,
    val height: Float = 100f
)

@Serializable
data class FlowConnection(
    val id: String = UUID.randomUUID().toString(),
    val fromNodeId: String,
    val toNodeId: String,
    val label: String? = null
)

@Serializable
data class FlowchartData(
    val nodes: List<FlowNode>,
    val connections: List<FlowConnection>
)

@Serializable
data class ContentBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val text: String? = null,
    val level: Int = 1,
    val items: List<ContentItem>? = null,
    val tableHeaders: List<String>? = null,
    val tableRows: List<List<String>>? = null,
    val imageUrl: String? = null,
    val tabs: List<TabItem>? = null,
    val flowchart: FlowchartData? = null,
    val ddItems: List<DifferentialDiagnosis>? = null,
    val tabName: String = "General",
    val videoId: String? = null,
    val videoTimestamps: List<VideoTimestamp>? = null,
    val variant: String? = null,
    val aiInstructions: String? = null,
    val linkedNoteId: String? = null,
    val linkedNoteTitle: String? = null
)

@Serializable
data class ListItem(
    val text: String,
    val subItems: List<ListItem>? = null 
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
)
