package com.algorithmx.medicine101.flowchart.ui

import androidx.compose.ui.geometry.Offset
//import com.medmate.flowchart.model.FlowConnection
//import com.medmate.flowchart.model.FlowNode

data class FlowchartState(
    // The "World" Data (The actual diagram)
    //val nodes: List<FlowNode> = emptyList(),
    //val connections: List<FlowConnection> = emptyList(),

    // The "Viewport" Data (The camera)
    val zoom: Float = 1f,
    val panOffset: Offset = Offset.Zero,

    // The "Interaction" Data
    val currentTool: Tool = Tool.PEN,
    val activeStroke: List<Offset> = emptyList(), // The line currently being drawn
    val selectedNodeId: String? = null // For editing/deleting
)

enum class Tool {
    HAND, // For panning/zooming
    PEN,  // For drawing shapes
    ERASER // For deleting
}
