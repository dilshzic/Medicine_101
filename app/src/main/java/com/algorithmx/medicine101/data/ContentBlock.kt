package com.algorithmx.medicine101.data

import kotlinx.serialization.Serializable

@Serializable
data class ContentBlock(
    val type: String,               // "header", "list", "table", "callout", "kv_list", "note_link"
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
data class FlowchartData(
    val direction: String = "vertical", 
    val nodes: List<FlowchartNode>,
    val connections: List<FlowchartConnection>
)

@Serializable
data class FlowchartNode(
    val id: String,
    val label: String,
    val type: String = "process", 
    val level: Int,               
    val order: Int                
)

@Serializable
data class FlowchartConnection(
    val from: String,
    val to: String,
    val label: String? = null     
)

@Serializable
data class DifferentialDiagnosis(
    val finding: String,         
    val diagnoses: List<String>, 
    val likelihood: String? = null 
)

@Serializable
data class ContentItem(
    val id: String = java.util.UUID.randomUUID().toString(),
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
