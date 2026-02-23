// In InkUtils.kt or inside RecognitionManager
import com.google.mlkit.vision.digitalink.Ink
import androidx.compose.ui.geometry.Offset

fun buildInkFromStrokes(points: List<Offset>): Ink {
    val builder = Ink.builder()
    val strokeBuilder = Ink.Stroke.builder()
    
    // We construct a single stroke from the list of points
    points.forEachIndexed { index, point ->
        strokeBuilder.addPoint(
            Ink.Point.create(point.x, point.y, index.toLong()) // Simple incremental time
        )
    }
    
    builder.addStroke(strokeBuilder.build())
    return builder.build()
}
