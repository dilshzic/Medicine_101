package com.medmate.flowchart.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FlowchartScreen(
    viewModel: FlowchartViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // We capture the "World Camera" variables
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    // Sync local state with ViewModel (optional, but good for performance)
    LaunchedEffect(state.zoom, state.panOffset) {
        zoom = state.zoom
        pan = state.panOffset
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // 1. Handle Zoom/Pan (Two Fingers)
            .pointerInput(Unit) {
                detectTransformGestures { _, panDelta, zoomDelta, _ ->
                    if (state.currentTool == Tool.HAND) {
                        viewModel.onEvent(FlowchartEvent.Pan(panDelta))
                        viewModel.onEvent(FlowchartEvent.Zoom(Offset.Zero, zoomDelta))
                    }
                }
            }
            // 2. Handle Drawing (One Finger)
            .pointerInput(state.currentTool) {
                if (state.currentTool == Tool.PEN) {
                    detectDragGestures(
                        onDragStart = { start -> 
                            viewModel.onEvent(FlowchartEvent.StartDrawing(start)) 
                        },
                        onDrag = { change, _ ->
                            change.consume() // Claim the touch
                            viewModel.onEvent(FlowchartEvent.UpdateDrawing(change.position))
                        },
                        onDragEnd = { 
                            viewModel.onEvent(FlowchartEvent.StopDrawing) 
                        }
                    )
                }
            }
    ) {
        // 3. The Drawing Engine
        Canvas(modifier = Modifier.fillMaxSize()) {
            // IMPORTANT: Apply the Camera Transform (Zoom/Pan)
            withTransform({
                translate(left = pan.x, top = pan.y)
                scale(scaleX = zoom, scaleY = zoom, pivot = Offset.Zero)
            }) {
                
                // A. Draw Connections (Lines) First
                state.connections.forEach { connection ->
                    // Logic to find start/end nodes would go here
                    // drawLine(...)
                }

                // B. Draw Nodes (Shapes)
                state.nodes.forEach { node ->
                    val color = if (state.selectedNodeId == node.id) Color.Blue else Color.Gray
                    
                    when (node.type) {
                        NodeType.RECTANGLE -> {
                            drawRect(
                                color = color,
                                topLeft = Offset(
                                    node.centerX - node.width / 2, 
                                    node.centerY - node.height / 2
                                ),
                                size = androidx.compose.ui.geometry.Size(node.width, node.height),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                        NodeType.DIAMOND -> {
                            // Draw diamond path
                            val path = Path().apply {
                                moveTo(node.centerX, node.centerY - node.height / 2) // Top
                                lineTo(node.centerX + node.width / 2, node.centerY) // Right
                                lineTo(node.centerX, node.centerY + node.height / 2) // Bottom
                                lineTo(node.centerX - node.width / 2, node.centerY) // Left
                                close()
                            }
                            drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
                        }
                        else -> { /* Draw Oval */ }
                    }
                }

                // C. Draw the "Active Ink" (What user is currently drawing)
                if (state.activeStroke.isNotEmpty()) {
                    val path = Path()
                    state.activeStroke.forEachIndexed { index, point ->
                        if (index == 0) path.moveTo(point.x, point.y)
                        else path.lineTo(point.x, point.y)
                    }
                    drawPath(
                        path = path,
                        color = Color.Red, // Make it distinct
                        style = Stroke(width = 5.dp.toPx()) // Thicker
                    )
                }
            }
        }
        
        // 4. Floating UI Controls (Tool Palette)
        // Add a Column here with Buttons to switch between PEN and HAND
    }
}
