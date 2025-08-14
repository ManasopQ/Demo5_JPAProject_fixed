package com.example.karaoke.audio

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import java.io.File
import kotlin.math.pow

/** Audio engine based on JavaFX's [MediaPlayer]. */
class JavaFXAudioEngine : AudioEngine {
    private var player: MediaPlayer? = null

    override fun load(file: File) {
        player?.dispose()
        player = MediaPlayer(Media(file.toURI().toString()))
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
}
