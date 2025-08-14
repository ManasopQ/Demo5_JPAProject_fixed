package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

/** Displays a simple RMS/peak level meter. */
@Composable
fun LevelMeter(rms: Float, peak: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.background(Color.Black)) {
        val w = size.width
        val h = size.height
        val rmsH = (rms.coerceIn(0f, 1f)) * h
        val peakH = (peak.coerceIn(0f, 1f)) * h
        drawRect(Color.Green, Offset(0f, h - rmsH), Size(w, rmsH))
        drawLine(Color.Red, Offset(0f, h - peakH), Offset(w, h - peakH), strokeWidth = 2f)
    }
}
