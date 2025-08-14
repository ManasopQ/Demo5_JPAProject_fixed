package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Draws a miniature waveform and a draggable playhead that can seek within the
 * track.
 */
@Composable
fun WaveformView(
    waveform: List<Float>,
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dragModifier = modifier
        .pointerInput(durationMs) {
            detectTapGestures { offset ->
                val ratio = offset.x / size.width
                onSeek((durationMs * ratio).toLong())
            }
        }
        .pointerInput(durationMs) {
            detectDragGestures { change, _ ->
                val ratio = change.position.x / size.width
                onSeek((durationMs * ratio).toLong())
            }
        }

    Canvas(dragModifier) {
        val w = size.width
        val h = size.height
        if (waveform.isNotEmpty()) {
            val step = (waveform.size / w).coerceAtLeast(1f)
            var x = 0f
            while (x < w) {
                val idx = (x * step).toInt().coerceIn(0, waveform.lastIndex)
                val amp = waveform[idx].coerceIn(0f, 1f)
                val y = amp * h / 2f
                drawLine(
                    Color.Gray,
                    Offset(x, h / 2 - y),
                    Offset(x, h / 2 + y),
                    strokeWidth = 1f
                )
                x += 1f
            }
        }
        val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
        val playX = progress * w
        drawLine(Color.Red, Offset(playX, 0f), Offset(playX, h), strokeWidth = 2f)
    }
}
