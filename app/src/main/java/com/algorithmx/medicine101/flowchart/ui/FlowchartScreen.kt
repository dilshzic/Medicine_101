package com.algorithmx.medicine101.flowchart.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.algorithmx.medicine101.data.FlowchartData
import com.algorithmx.medicine101.data.NodeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowchartScreen(
    initialData: FlowchartData?,
    onDone: (FlowchartData) -> Unit,
    onBack: () -> Unit,
    viewModel: FlowchartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(initialData) {
        viewModel.setInitialData(initialData)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drawing Canvas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onDone(viewModel.getFlowchartData()) }) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTransformGestures { _, panDelta, zoomDelta, _ ->
                        if (state.currentTool == Tool.HAND) {
                            viewModel.onEvent(FlowchartEvent.Pan(panDelta))
                            viewModel.onEvent(FlowchartEvent.Zoom(Offset.Zero, zoomDelta))
                        }
                    }
                }
                .pointerInput(state.currentTool) {
                    if (state.currentTool == Tool.PEN) {
                        detectDragGestures(
                            onDragStart = { start ->
                                viewModel.onEvent(FlowchartEvent.StartDrawing(start))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                viewModel.onEvent(FlowchartEvent.UpdateDrawing(change.position))
                            },
                            onDragEnd = {
                                viewModel.onEvent(FlowchartEvent.StopDrawing)
                            }
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                withTransform({
                    translate(left = state.panOffset.x, top = state.panOffset.y)
                    scale(scaleX = state.zoom, scaleY = state.zoom, pivot = Offset.Zero)
                }) {

                    state.connections.forEach { connection ->
                        val fromNode = state.nodes.find { it.id == connection.fromNodeId }
                        val toNode = state.nodes.find { it.id == connection.toNodeId }
                        if (fromNode != null && toNode != null) {
                            drawLine(
                                color = Color.Gray,
                                start = Offset(fromNode.centerX, fromNode.centerY),
                                end = Offset(toNode.centerX, toNode.centerY),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }

                    state.nodes.forEach { node ->
                        val color = if (state.selectedNodeId == node.id) Color.Blue else Color.Gray
                        val rectTopLeft = Offset(node.centerX - node.width / 2, node.centerY - node.height / 2)

                        when (node.type) {
                            NodeType.RECTANGLE -> {
                                drawRect(
                                    color = color,
                                    topLeft = rectTopLeft,
                                    size = Size(node.width, node.height),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            NodeType.DIAMOND -> {
                                val path = Path().apply {
                                    moveTo(node.centerX, rectTopLeft.y)
                                    lineTo(rectTopLeft.x + node.width, node.centerY)
                                    lineTo(node.centerX, rectTopLeft.y + node.height)
                                    lineTo(rectTopLeft.x, node.centerY)
                                    close()
                                }
                                drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
                            }
                            NodeType.OVAL -> {
                                drawOval(
                                    color = color,
                                    topLeft = rectTopLeft,
                                    size = Size(node.width, node.height),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            else -> {}
                        }

                        if (node.text.isNotEmpty()) {
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = Paint().apply {
                                    this.color = android.graphics.Color.BLACK
                                    textSize = 30f / state.zoom
                                    textAlign = Paint.Align.CENTER
                                }
                                drawText(node.text, node.centerX, node.centerY + 10f, paint)
                            }
                        }
                    }

                    state.activeStrokes.forEach { stroke ->
                        if (stroke.isNotEmpty()) {
                            val path = Path()
                            stroke.forEachIndexed { index, point ->
                                if (index == 0) path.moveTo(point.x, point.y)
                                else path.lineTo(point.x, point.y)
                            }
                            drawPath(path, Color.Red, style = Stroke(width = 3.dp.toPx()))
                        }
                    }
                }
            }

            // TOOL PALETTE
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
                    IconButton(
                        onClick = { viewModel.onEvent(FlowchartEvent.SelectTool(Tool.PEN)) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (state.currentTool == Tool.PEN) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (state.currentTool == Tool.PEN) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Draw Tool")
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(FlowchartEvent.SelectTool(Tool.HAND)) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (state.currentTool == Tool.HAND) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (state.currentTool == Tool.HAND) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.PanTool, contentDescription = "Pan Tool")
                    }
                }
            }
        }
    }
}
