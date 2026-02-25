package com.algorithmx.medicine101.ui.screens.noteview.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.algorithmx.medicine101.data.FlowchartData
import com.algorithmx.medicine101.data.NodeType

@Composable
fun FlowchartRenderer(data: FlowchartData) {
    if (data.nodes.isEmpty()) return

    // 1. Calculate the bounding box of the hand-drawn content
    val minX = data.nodes.minOf { it.centerX - it.width / 2 }
    val maxX = data.nodes.maxOf { it.centerX + it.width / 2 }
    val minY = data.nodes.minOf { it.centerY - it.height / 2 }
    val maxY = data.nodes.maxOf { it.centerY + it.height / 2 }

    val contentWidth = (maxX - minX) + 100f
    val contentHeight = (maxY - minY) + 100f

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .horizontalScroll(scrollState)
    ) {
        Canvas(modifier = Modifier.size(contentWidth.dp, contentHeight.dp)) {
            // Apply translation to center the content if it's smaller than the box
            val offsetX = 50f - minX
            val offsetY = 50f - minY

            // A. Draw Connections
            data.connections.forEach { conn ->
                val fromNode = data.nodes.find { it.id == conn.fromNodeId }
                val toNode = data.nodes.find { it.id == conn.toNodeId }

                if (fromNode != null && toNode != null) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(fromNode.centerX + offsetX, fromNode.centerY + offsetY),
                        end = Offset(toNode.centerX + offsetX, toNode.centerY + offsetY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // B. Draw Nodes
            data.nodes.forEach { node ->
                val rectTopLeft = Offset(
                    node.centerX - node.width / 2 + offsetX,
                    node.centerY - node.height / 2 + offsetY
                )
                
                when (node.type) {
                    NodeType.RECTANGLE -> {
                        drawRect(
                            color = Color.Gray,
                            topLeft = rectTopLeft,
                            size = Size(node.width, node.height),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    NodeType.DIAMOND -> {
                        val path = Path().apply {
                            moveTo(node.centerX + offsetX, rectTopLeft.y)
                            lineTo(rectTopLeft.x + node.width, node.centerY + offsetY)
                            lineTo(node.centerX + offsetX, rectTopLeft.y + node.height)
                            lineTo(rectTopLeft.x, node.centerY + offsetY)
                            close()
                        }
                        drawPath(path, Color.Gray, style = Stroke(width = 2.dp.toPx()))
                    }
                    NodeType.OVAL -> {
                        drawOval(
                            color = Color.Gray,
                            topLeft = rectTopLeft,
                            size = Size(node.width, node.height),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    else -> {}
                }

                // C. Draw Node Text
                if (node.text.isNotEmpty()) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 30f
                            textAlign = Paint.Align.CENTER
                        }
                        drawText(
                            node.text,
                            node.centerX + offsetX,
                            node.centerY + offsetY + 10f,
                            paint
                        )
                    }
                }
            }
        }
    }
}
