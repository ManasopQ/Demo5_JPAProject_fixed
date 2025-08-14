package com.example.karaoke.audio

import java.io.File

/** Simple abstraction for audio playback so different engines can be swapped. */
interface AudioEngine {
    /** Load [file] as the current track. */
    fun load(file: File)

    /** Start or resume playback. */
    fun play()

    /** Pause playback. */
    fun pause()

    /** Stop playback and reset position to start. */
    fun stop()

    /** Seek to [positionMs] in milliseconds. */
    fun seek(positionMs: Long)

    /** Current playback position in milliseconds. */
    fun positionMs(): Long

    /** Set playback tempo (1.0 = normal speed). */
    fun setTempo(tempo: Float)

    /**
     * Set pitch shift in semitones. 0f is original pitch, positive raises,
     * negative lowers. The range is expected to be within ±6 semitones.
     */
    fun setPitch(semitones: Float)

    /**
     * Register [listener] to receive real-time RMS and peak levels in the
     * range 0.0–1.0. Pass `null` to remove the listener.
     */
    fun setLevelListener(listener: ((rms: Float, peak: Float) -> Unit)?)
}

/** Available audio backends. */
enum class EngineType { JavaFX, TarsosDSP }
