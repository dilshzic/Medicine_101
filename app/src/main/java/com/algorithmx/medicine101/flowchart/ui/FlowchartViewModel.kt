package com.algorithmx.medicine101.flowchart.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import com.algorithmx.medicine101.flowchart.ml.buildInkFromStrokes
import com.algorithmx.medicine101.flowchart.ml.buildInkFromMultiStrokes
import com.algorithmx.medicine101.data.FlowNode
import com.algorithmx.medicine101.data.NodeType
import com.algorithmx.medicine101.data.FlowConnection
import com.algorithmx.medicine101.data.FlowchartData
import com.algorithmx.medicine101.flowchart.ml.RecognitionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FlowchartViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(FlowchartState())
    val state = _state.asStateFlow()

    private var recognitionJob: Job? = null
    private val DEBOUNCE_DELAY = 800L
    
    private var dragStartOffset: Offset? = null
    private var originalNodePosition: Offset? = null

    fun setInitialData(data: FlowchartData?) {
        if (data == null) return
        _state.update { it.copy(nodes = data.nodes, connections = data.connections) }
    }

    fun getFlowchartData(): FlowchartData {
        return FlowchartData(
            nodes = _state.value.nodes,
            connections = _state.value.connections
        )
    }

    fun onEvent(event: FlowchartEvent) {
        when (event) {
            is FlowchartEvent.Pan -> {
                _state.update { it.copy(panOffset = it.panOffset + event.delta) }
            }

            is FlowchartEvent.Zoom -> {
                _state.update {
                    val newZoom = (it.zoom * event.zoomChange).coerceIn(0.5f, 3f)
                    it.copy(zoom = newZoom)
                }
            }

            is FlowchartEvent.SelectTool -> {
                _state.update { it.copy(currentTool = event.tool, selectedNodeId = null) }
            }

            is FlowchartEvent.StartDrawing -> {
                val worldPoint = screenToWorld(event.startPoint)
                when (_state.value.currentTool) {
                    Tool.PEN -> {
                        recognitionJob?.cancel()
                        _state.update { 
                            val currentStrokes = it.activeStrokes + listOf(listOf(worldPoint))
                            it.copy(activeStrokes = currentStrokes) 
                        }
                    }
                    Tool.SELECT -> {
                        val node = findNodeAt(worldPoint)
                        if (node != null) {
                            dragStartOffset = worldPoint
                            originalNodePosition = Offset(node.centerX, node.centerY)
                            _state.update { it.copy(selectedNodeId = node.id, isDraggingNode = true) }
                        } else {
                            _state.update { it.copy(selectedNodeId = null) }
                        }
                    }
                    Tool.ERASER -> {
                        val node = findNodeAt(worldPoint)
                        if (node != null) {
                            deleteNode(node.id)
                        } else {
                            // Try to find connection near point
                            findConnectionAt(worldPoint)?.let { deleteConnection(it.id) }
                        }
                    }
                    else -> {}
                }
            }

            is FlowchartEvent.UpdateDrawing -> {
                val worldPoint = screenToWorld(event.newPoint)
                when (_state.value.currentTool) {
                    Tool.PEN -> {
                        _state.update { state ->
                            val lastStrokeIndex = state.activeStrokes.lastIndex
                            if (lastStrokeIndex >= 0) {
                                val updatedLastStroke = state.activeStrokes[lastStrokeIndex] + worldPoint
                                val newStrokes = state.activeStrokes.toMutableList().apply {
                                    set(lastStrokeIndex, updatedLastStroke)
                                }
                                state.copy(activeStrokes = newStrokes)
                            } else state
                        }
                    }
                    Tool.SELECT -> {
                        if (_state.value.isDraggingNode && _state.value.selectedNodeId != null) {
                            val delta = worldPoint - (dragStartOffset ?: worldPoint)
                            val newPos = (originalNodePosition ?: Offset.Zero) + delta
                            updateNodePosition(_state.value.selectedNodeId!!, newPos)
                        }
                    }
                    else -> {}
                }
            }

            is FlowchartEvent.StopDrawing -> {
                when (_state.value.currentTool) {
                    Tool.PEN -> {
                        recognitionJob = viewModelScope.launch {
                            delay(DEBOUNCE_DELAY)
                            commitStrokes() 
                        }
                    }
                    Tool.SELECT -> {
                        _state.update { it.copy(isDraggingNode = false) }
                        dragStartOffset = null
                        originalNodePosition = null
                    }
                    else -> {}
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

    private fun mapToNodeType(label: String): NodeType {
        return when (label.lowercase()) {
            "rectangle", "square", "box" -> NodeType.RECTANGLE
            "diamond", "rhombus" -> NodeType.DIAMOND
            "circle", "oval", "ellipse" -> NodeType.OVAL
            else -> NodeType.UNKNOWN
        }
    }

    private fun findNodeAt(point: Offset): FlowNode? {
        return _state.value.nodes.find { node ->
            point.x in (node.centerX - node.width/2)..(node.centerX + node.width/2) &&
            point.y in (node.centerY - node.height/2)..(node.centerY + node.height/2)
        }
    }

    private fun findConnectionAt(point: Offset): FlowConnection? {
        // Simple distance-to-line segment check could be implemented here
        // For now, let's just return null or check nodes
        return null 
    }

    private fun deleteNode(id: String) {
        _state.update { state ->
            state.copy(
                nodes = state.nodes.filter { it.id != id },
                connections = state.connections.filter { it.fromNodeId != id && it.toNodeId != id }
            )
        }
    }

    private fun deleteConnection(id: String) {
        _state.update { it.copy(connections = it.connections.filter { it.id != id }) }
    }

    private fun updateNodePosition(id: String, position: Offset) {
        _state.update { state ->
            val updatedNodes = state.nodes.map { 
                if (it.id == id) it.copy(centerX = position.x, centerY = position.y) else it 
            }
            state.copy(nodes = updatedNodes)
        }
    }

    private fun commitStrokes() {
        val allStrokes = _state.value.activeStrokes
        val allPoints = allStrokes.flatten()
        
        if (allPoints.isEmpty()) return

        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }
        val centerX = (minX + maxX) / 2
        val centerY = (minY + maxY) / 2

        val targetNode = findNodeAt(Offset(centerX, centerY))

        if (targetNode != null) {
            val ink = buildInkFromMultiStrokes(allStrokes)
            RecognitionManager.recognizeText(ink) { text ->
                val updatedText = if (targetNode.text.isEmpty()) text else "${targetNode.text} $text"
                updateNodeText(targetNode.id, updatedText)
            }
        } else {
            recognizeShape(allPoints, allStrokes) 
        }

        _state.update { it.copy(activeStrokes = emptyList()) }
    }

    private fun updateNodeText(nodeId: String, newText: String) {
        _state.update { currentState ->
            val updatedNodes = currentState.nodes.map { node ->
                if (node.id == nodeId) node.copy(text = newText) else node
            }
            currentState.copy(nodes = updatedNodes)
        }
    }

    private fun recognizeShape(points: List<Offset>, strokes: List<List<Offset>>) {
        if (points.size < 10) return

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }

        val centerX = (minX + maxX) / 2
        val centerY = (minY + maxY) / 2
        val width = (maxX - minX).coerceAtLeast(100f)
        val height = (maxY - minY).coerceAtLeast(60f)

        val ink = if (strokes.size > 1) buildInkFromMultiStrokes(strokes) else buildInkFromStrokes(points)

        RecognitionManager.recognizeShape(ink) { label, _ ->
            if (label.contains("arrow")) {
                val startPoint = points.first()
                val endPoint = points.last()
                
                val fromNode = findNodeAt(startPoint)
                val toNode = findNodeAt(endPoint)
                
                if (fromNode != null && toNode != null && fromNode.id != toNode.id) {
                    val newConnection = FlowConnection(
                        fromNodeId = fromNode.id,
                        toNodeId = toNode.id
                    )
                    _state.update { 
                        it.copy(connections = it.connections + newConnection) 
                    }
                }
            } else {
                val detectedType = mapToNodeType(label)
                if (detectedType != NodeType.UNKNOWN) {
                    val newNode = FlowNode(
                        type = detectedType,
                        text = "",
                        centerX = centerX,
                        centerY = centerY,
                        width = width,
                        height = height
                    )
                    _state.update {
                        it.copy(nodes = it.nodes + newNode)
                    }
                }
            }
        }
    }
}
