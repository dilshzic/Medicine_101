package com.algorithmx.medicine101.flowchart.ui

import androidx.compose.ui.geometry.Offset

sealed class FlowchartEvent {
    // Canvas Gestures
    data class Pan(val delta: Offset) : FlowchartEvent()
    data class Zoom(val centroid: Offset, val zoomChange: Float) : FlowchartEvent()
    
    // Drawing Actions
    data class StartDrawing(val startPoint: Offset) : FlowchartEvent()
    data class UpdateDrawing(val newPoint: Offset) : FlowchartEvent()
    object StopDrawing : FlowchartEvent() // This triggers the AI Recognition!
    
    // UI Controls
    data class SelectTool(val tool: Tool) : FlowchartEvent()
    data class SelectNode(val nodeId: String) : FlowchartEvent()
}
