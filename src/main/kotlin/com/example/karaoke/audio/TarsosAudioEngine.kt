package com.example.karaoke.audio

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.effects.PitchShifter
import be.tarsos.dsp.effects.RateTransposer
import be.tarsos.dsp.GainProcessor
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.io.jvm.AudioPlayer
import java.io.File
import javax.sound.sampled.AudioFormat
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/** Audio engine using the TarsosDSP library. */
class TarsosAudioEngine : AudioEngine {
    private var dispatcher: AudioDispatcher? = null
    private var playThread: Thread? = null
    private var currentFile: File? = null
    private var tempo: Float = 1f
    private var pitch: Float = 0f
    private var position: Long = 0L
    private var levelListener: ((Float, Float) -> Unit)? = null
    private var volume: Float = 1f

    private val sampleRate = 44_100
    private val bufferSize = 2048
    private val overlap = 0

    override fun load(file: File) {
        currentFile = file
        buildDispatcher(0L)
    }

    private fun buildDispatcher(startMs: Long) {
        dispatcher?.stop()
        position = startMs
        val f = currentFile ?: return
        val d = AudioDispatcherFactory.fromPipe(f.absolutePath, sampleRate, bufferSize, overlap)
        val rate = RateTransposer(tempo.toDouble())
        val shift = PitchShifter(2f.pow(pitch / 12f), bufferSize, overlap)
        d.addAudioProcessor(rate)
        d.addAudioProcessor(shift)
        d.addAudioProcessor(GainProcessor(volume.toDouble()))
        d.addAudioProcessor(object : AudioProcessor {
            override fun process(event: AudioEvent): Boolean {
                val buffer = event.floatBuffer
                var peak = 0f
                var sum = 0f
                for (s in buffer) {
                    val a = abs(s)
                    sum += a * a
                    if (a > peak) peak = a
                }
                val rms = sqrt(sum / buffer.size)
                levelListener?.invoke(rms, peak)
                position += (1000L * event.bufferSize / sampleRate)
                return true
            }

            override fun processingFinished() {}
        })
        val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        d.addAudioProcessor(AudioPlayer(format))
        dispatcher = d
    }

    override fun play() {
        val d = dispatcher ?: return
        playThread = thread(start = true, isDaemon = true) { d.run() }
    }

    override fun pause() {
        dispatcher?.stop()
        playThread?.join()
    }

    override fun stop() {
        dispatcher?.stop()
        playThread?.join()
        position = 0L
    }

    override fun seek(positionMs: Long) {
        buildDispatcher(positionMs)
    }

    override fun positionMs(): Long = position

    override fun setTempo(tempo: Float) {
        this.tempo = tempo
        buildDispatcher(position)
    }

    override fun setPitch(semitones: Float) {
        this.pitch = semitones
        buildDispatcher(position)
    }

    override fun setLevelListener(listener: ((rms: Float, peak: Float) -> Unit)?) {
        levelListener = listener
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
        buildDispatcher(position)
    }
}
