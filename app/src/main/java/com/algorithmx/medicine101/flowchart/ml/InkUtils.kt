package com.algorithmx.medicine101.flowchart.ml

import androidx.compose.ui.geometry.Offset
import com.google.mlkit.vision.digitalink.recognition.Ink

fun buildInkFromStrokes(points: List<Offset>): Ink {
    val builder = Ink.builder()
    val strokeBuilder = Ink.Stroke.builder()

    points.forEachIndexed { index, point ->
        strokeBuilder.addPoint(
            Ink.Point.create(point.x, point.y, index.toLong())
        )
    }

    builder.addStroke(strokeBuilder.build())
    return builder.build()
}

fun buildInkFromMultiStrokes(strokes: List<List<Offset>>): Ink {
    val builder = Ink.builder()
    
    strokes.forEachIndexed { strokeIndex, points ->
        val strokeBuilder = Ink.Stroke.builder()
        points.forEachIndexed { pointIndex, point ->
            val t = (strokeIndex * 1000L) + pointIndex
            strokeBuilder.addPoint(Ink.Point.create(point.x, point.y, t))
        }
        builder.addStroke(strokeBuilder.build())
    }
    
    return builder.build()
}
