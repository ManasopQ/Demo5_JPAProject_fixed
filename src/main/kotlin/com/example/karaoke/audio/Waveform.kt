package com.example.karaoke.audio

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import java.io.File
import kotlin.math.abs

/**
 * Utility to extract a simple waveform peak envelope from [file]. The result
 * contains [points] values in the range 0.0–1.0 representing peaks across the
 * track duration.
 */
fun extractWaveform(file: File, points: Int = 1000): List<Float> {
    val dispatcher = AudioDispatcherFactory.fromPipe(file.absolutePath, 44_100, 2048, 0)
    val peaks = mutableListOf<Float>()
    dispatcher.addAudioProcessor(object : AudioProcessor {
        override fun process(event: AudioEvent): Boolean {
            var peak = 0f
            for (s in event.floatBuffer) {
                val a = abs(s)
                if (a > peak) peak = a
            }
            peaks += peak
            return true
        }

        override fun processingFinished() {}
    })
    dispatcher.run()

    if (peaks.isEmpty()) return emptyList()
    if (peaks.size <= points) return peaks
    val result = MutableList(points) { 0f }
    val ratio = peaks.size.toFloat() / points
    for (i in peaks.indices) {
        val idx = (i / ratio).toInt()
        if (peaks[i] > result[idx]) result[idx] = peaks[i]
    }
    return result
}
