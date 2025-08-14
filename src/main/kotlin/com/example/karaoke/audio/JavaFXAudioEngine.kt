package com.example.karaoke.audio

import javafx.scene.media.AudioSpectrumListener
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import java.io.File
import kotlin.math.pow
import kotlin.math.sqrt

/** Audio engine based on JavaFX's [MediaPlayer]. */
class JavaFXAudioEngine : AudioEngine {
    private var player: MediaPlayer? = null
    private var levelListener: ((Float, Float) -> Unit)? = null

    override fun load(file: File) {
        player?.dispose()
        player = MediaPlayer(Media(file.toURI().toString())).apply {
            audioSpectrumInterval = 0.1
            audioSpectrumNumBands = 64
            audioSpectrumListener = AudioSpectrumListener { _, _, magnitudes, _ ->
                var peak = 0f
                var sum = 0f
                for (m in magnitudes) {
                    val amp = 10f.pow(m / 20f)
                    sum += amp * amp
                    if (amp > peak) peak = amp
                }
                val rms = sqrt(sum / magnitudes.size)
                levelListener?.invoke(rms, peak)
            }
        }
    }

    override fun play() {
        player?.play()
    }

    override fun pause() {
        player?.pause()
    }

    override fun stop() {
        player?.stop()
    }

    override fun seek(positionMs: Long) {
        player?.seek(Duration.millis(positionMs.toDouble()))
    }

    override fun positionMs(): Long = player?.currentTime?.toMillis()?.toLong() ?: 0L

    override fun setTempo(tempo: Float) {
        player?.rate = tempo.toDouble()
    }

    override fun setPitch(semitones: Float) {
        // JavaFX MediaPlayer cannot change pitch independently. Adjust rate
        // to approximate the requested pitch shift.
        val factor = 2.0.pow(semitones / 12.0)
        player?.rate = factor
    }

    override fun setLevelListener(listener: ((rms: Float, peak: Float) -> Unit)?) {
        levelListener = listener
    }

    override fun setVolume(volume: Float) {
        player?.volume = volume.toDouble()
    }
}
