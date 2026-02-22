package com.algorithmx.medicine101.ui.screens.noteview.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithmx.medicine101.data.FlowchartData
import com.algorithmx.medicine101.data.FlowchartNode

// --- Configuration Constants ---
// Adjust these to change the spacing of your diagram
private val NODE_WIDTH = 120.dp
private val NODE_HEIGHT = 55.dp
private val X_SPACING = 150.dp // Horizontal distance between columns
private val Y_SPACING = 100.dp // Vertical distance between rows

@Composable
fun FlowchartRenderer(data: FlowchartData) {
    // 1. Calculate the total size required for the canvas
    val maxLevel = data.nodes.maxOfOrNull { it.level } ?: 0
    val maxOrder = data.nodes.maxOfOrNull { it.order } ?: 0

    // Add some padding to the calculations
    val totalWidth = X_SPACING * (maxOrder + 1) + 50.dp
    val totalHeight = Y_SPACING * (maxLevel + 1) + 100.dp

    // Map for easy lookup of nodes by ID
    val nodeMap = remember(data) { data.nodes.associateBy { it.id } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(450.dp) // Fixed height container
            .background(Color(0xFFFAFAFA))
            .horizontalScroll(rememberScrollState()) // Allow scrolling for wide charts
    ) {
        Box(
            modifier = Modifier.size(width = totalWidth, height = totalHeight)
        ) {
            // LAYER 1: Connections (Lines)
            // Drawn first so they appear BEHIND the nodes
            Canvas(modifier = Modifier.fillMaxSize()) {
                data.connections.forEach { conn ->
                    val startNode = nodeMap[conn.from]
                    val endNode = nodeMap[conn.to]

                    if (startNode != null && endNode != null) {
                        // Calculate EXACT centers based on Grid Logic
                        val startX = (startNode.order * X_SPACING.toPx()) + (NODE_WIDTH.toPx() / 2) + 20.dp.toPx()
                        val startY = (startNode.level * Y_SPACING.toPx()) + NODE_HEIGHT.toPx() + 20.dp.toPx()

                        val endX = (endNode.order * X_SPACING.toPx()) + (NODE_WIDTH.toPx() / 2) + 20.dp.toPx()
                        val endY = (endNode.level * Y_SPACING.toPx()) + 20.dp.toPx() // Top of end node

                        // Draw Curve
                        val path = Path().apply {
                            moveTo(startX, startY)
                            cubicTo(
                                startX, startY + 50f, // Control point 1 (down)
                                endX, endY - 50f,     // Control point 2 (up)
                                endX, endY
                            )
                        }

                        drawPath(
                            path = path,
                            color = Color.Gray,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            // LAYER 2: Connection Labels (Optional)
            data.connections.forEach { conn ->
                if (!conn.label.isNullOrEmpty()) {
                    val startNode = nodeMap[conn.from]
                    val endNode = nodeMap[conn.to]

                    if (startNode != null && endNode != null) {
                        val startX = (startNode.order * X_SPACING.value) + (NODE_WIDTH.value / 2) + 20f
                        val endX = (endNode.order * X_SPACING.value) + (NODE_WIDTH.value / 2) + 20f
                        val startY = (startNode.level * Y_SPACING.value) + NODE_HEIGHT.value + 20f
                        val endY = (endNode.level * Y_SPACING.value) + 20f

                        val labelX = (startX + endX) / 2
                        val labelY = (startY + endY) / 2

                        Text(
                            text = conn.label,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .offset(x = labelX.dp, y = labelY.dp)
                                .background(Color.White)
                                .padding(2.dp)
                        )
                    }
                }
            }

            // LAYER 3: Nodes
            data.nodes.forEach { node ->
                FlowchartNodeBox(
                    node = node,
                    modifier = Modifier
                        .offset(
                            x = (node.order * X_SPACING.value).dp + 20.dp,
                            y = (node.level * Y_SPACING.value).dp + 20.dp
                        )
                )
            }
        }
    }
}

@Composable
fun FlowchartNodeBox(node: FlowchartNode, modifier: Modifier = Modifier) {
    val (bgColor, borderColor) = when (node.type) {
        "start", "end" -> Color(0xFFE3F2FD) to Color(0xFF2196F3) // Blue
        "decision" -> Color(0xFFFFF3E0) to Color(0xFFFF9800)     // Orange
        else -> Color.White to Color.Gray                        // Default
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(NODE_WIDTH)
            .height(NODE_HEIGHT)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Text(
            text = node.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            color = Color.Black
        )
    }
}