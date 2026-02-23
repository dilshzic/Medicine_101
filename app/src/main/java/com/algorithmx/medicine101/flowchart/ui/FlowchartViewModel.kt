package com.medmate.flowchart.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medmate.flowchart.model.FlowNode
import com.medmate.flowchart.model.NodeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.launch

class FlowchartViewModel : ViewModel() {

    private val _state = MutableStateFlow(FlowchartState())
    val state = _state.asStateFlow()

    fun onEvent(event: FlowchartEvent) {
        when (event) {
            is FlowchartEvent.Pan -> {
                _state.update { it.copy(panOffset = it.panOffset + event.delta) }
            }
            
            is FlowchartEvent.Zoom -> {
                // Advanced: We will implement "zoom towards finger" logic later.
                // For now, simple zoom is enough.
                _state.update { 
                    val newZoom = (it.zoom * event.zoomChange).coerceIn(0.5f, 3f)
                    it.copy(zoom = newZoom) 
                }
            }

            is FlowchartEvent.SelectTool -> {
                _state.update { it.copy(currentTool = event.tool) }
            }

            is FlowchartEvent.StartDrawing -> {
                if (_state.value.currentTool == Tool.PEN) {
                    // Convert Screen Coordinate to World Coordinate
                    val worldPoint = screenToWorld(event.startPoint)
                    _state.update { it.copy(activeStroke = listOf(worldPoint)) }
                }
            }

            is FlowchartEvent.UpdateDrawing -> {
                if (_state.value.currentTool == Tool.PEN) {
                    val worldPoint = screenToWorld(event.newPoint)
                    _state.update { it.copy(activeStroke = it.activeStroke + worldPoint) }
                }
            }

            is FlowchartEvent.StopDrawing -> {
                if (_state.value.currentTool == Tool.PEN) {
                    recognizeShape(_state.value.activeStroke)
                    _state.update { it.copy(activeStroke = emptyList()) }
                }
            }
            
            // ... handle other events
            else -> {}
        }
    }

    // --- Helper Math Functions ---
    // These are crucial for the "Infinite Canvas" to work
    
    private fun screenToWorld(screenOffset: Offset): Offset {
        val zoom = _state.value.zoom
        val pan = _state.value.panOffset
        // Formula: (Screen - Pan) / Zoom
        return (screenOffset - pan) / zoom
    }

    // --- AI Placeholder ---
    private fun recognizeShape(points: List<Offset>) {
        // We will hook up ML Kit here in Phase 3.
        // For now, let's just pretend we recognized a rectangle for testing.
        if (points.size < 10) return // Ignore tiny taps
        
        // Calculate bounding box of the stroke
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        
        val newWidth = maxX - minX
        val newHeight = maxY - minY
        val centerX = minX + (newWidth / 2)
        val centerY = minY + (newHeight / 2)

        val newNode = FlowNode(
            type = NodeType.RECTANGLE, // Hardcoded for now
            text = "New Step",
            centerX = centerX,
            centerY = centerY,
            width = newWidth.coerceAtLeast(100f),
            height = newHeight.coerceAtLeast(50f)
        )

        _state.update { 
            it.copy(nodes = it.nodes + newNode) 
        }
    }
}
