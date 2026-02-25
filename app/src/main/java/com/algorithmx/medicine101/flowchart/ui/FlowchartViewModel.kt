package com.algorithmx.medicine101.flowchart.ui

/* 
 * SHELVED: Flowchart implementation is currently paused to prevent build errors.
 * 
import androidx.lifecycle.ViewModel
import com.algorithmx.medicine101.flowchart.model.FlowNode
import com.algorithmx.medicine101.flowchart.model.NodeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.ui.geometry.Offset

class FlowchartViewModel : ViewModel() {

    private val _state = MutableStateFlow(FlowchartState())
    val state = _state.asStateFlow()

    fun onEvent(event: FlowchartEvent) {
        when (event) {
            is FlowchartEvent.Pan -> {
                _state.update { it.copy(panOffset = it.panOffset + event.delta) }
            }
             FlowchartEvent.StartDrawing -> {
                if (_state.value.currentTool == Tool.PEN) {
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
            
            else -> {}
        }
    }

    private fun screenToWorld(screenOffset: Offset): Offset {
        val zoom = _state.value.zoom
        val pan = _state.value.panOffset
        return (screenOffset - pan) / zoom
    }

    private fun recognizeShape(points: List<Offset>) {
        if (points.size < 10) return
        
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        
        val newWidth = maxX - minX
        val newHeight = maxY - minY
        val centerX = minX + (newWidth / 2)
        val centerY = minY + (newHeight / 2)

        val newNode = FlowNode(
            id = java.util.UUID.randomUUID().toString(),
            type = NodeType.RECTANGLE,
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
*/
