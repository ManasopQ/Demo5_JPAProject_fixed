import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ui.LyricLine
import ui.LyricView
import ui.TapSyncPanel
import ui.PlaylistPanel
import ui.WaveformView
import ui.LevelMeter
import ui.SettingsDialog
import com.example.karaoke.lyrics.LrcWriter
import com.example.karaoke.lyrics.LrcReader
import com.example.karaoke.model.PlaylistStorage
import com.example.karaoke.model.Track
import com.example.karaoke.model.Settings
import com.example.karaoke.model.SettingsStorage
import com.example.karaoke.model.Theme as AppTheme
import com.example.karaoke.audio.AudioEngine
import com.example.karaoke.audio.EngineType
import com.example.karaoke.audio.JavaFXAudioEngine
import com.example.karaoke.audio.TarsosAudioEngine
import com.example.karaoke.audio.extractWaveform
import java.io.File
import java.awt.FileDialog
import java.awt.Frame

fun main() = application {
    var showTapSync by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showPlaylist by remember { mutableStateOf(true) }
    val settingsFile = File("settings.json")
    var settings by remember { mutableStateOf(SettingsStorage.load(settingsFile)) }

    val playlistFile = File("playlist.json")
    val playlist = remember { mutableStateListOf<Track>() }
    var currentTrackIndex by remember { mutableStateOf(-1) }
    var lyrics by remember {
        mutableStateOf(
            listOf(
                LyricLine(0L, "Line 1"),
                LyricLine(3000L, "Line 2"),
                LyricLine(6000L, "Line 3"),
                LyricLine(9000L, "Line 4")
            )
        )
    }
    var currentTime by remember { mutableStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }
    var engineType by remember { mutableStateOf(settings.defaultEngine) }
    var audioEngine by remember {
        mutableStateOf<AudioEngine>(
            when (settings.defaultEngine) {
                EngineType.JavaFX -> JavaFXAudioEngine()
                EngineType.TarsosDSP -> TarsosAudioEngine()
            }
        )
    }
    var tempo by remember { mutableStateOf(1f) }
    var pitch by remember { mutableStateOf(0f) }
    var volume by remember { mutableStateOf(settings.defaultVolume) }
    var lyricsFontSize by remember { mutableStateOf(settings.lyricsFontSize) }
    val tapTimes = remember { mutableStateListOf<Long>() }
    var tapRunning by remember { mutableStateOf(false) }
    var waveform by remember { mutableStateOf<List<Float>>(emptyList()) }
    var rmsLevel by remember { mutableStateOf(0f) }
    var peakLevel by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) { playlist.addAll(PlaylistStorage.load(playlistFile)) }
    fun savePlaylist() = PlaylistStorage.save(playlistFile, playlist)

    fun openAudio() {
        val fd = FileDialog(null as Frame?, "Select audio", FileDialog.LOAD)
        fd.isVisible = true
        val file = fd.file ?: return
        val path = fd.directory + file
        val track = Track(path, "", File(path).nameWithoutExtension, artist = "", durationMs = 30_000)
        playlist.add(track)
        savePlaylist()
        currentTrackIndex = playlist.lastIndex
        currentTime = 0L
        audioEngine.load(File(path))
    }

    fun openLyrics() {
        val fd = FileDialog(null as Frame?, "Select lyrics", FileDialog.LOAD)
        fd.isVisible = true
        val file = fd.file ?: return
        val path = fd.directory + file
        lyrics = LrcReader.read(File(path))
    }

    fun parseColor(hex: String): Color {
        return try {
            val c = java.awt.Color.decode(hex)
            Color(c.red, c.green, c.blue)
        } catch (e: Exception) {
            Color(0xFF6200EE)
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Lyrics",
        onPreviewKeyEvent = {
            if (it.type == KeyEventType.KeyDown) {
                when {
                    it.key == Key.Spacebar -> {
                        if (isPlaying) audioEngine.pause() else audioEngine.play()
                        isPlaying = !isPlaying
                        true
                    }
                    it.key == Key.DirectionLeft -> {
                        currentTime = (currentTime - 5000).coerceAtLeast(0)
                        audioEngine.seek(currentTime)
                        true
                    }
                    it.key == Key.DirectionRight -> {
                        val duration = playlist.getOrNull(currentTrackIndex)?.durationMs ?: Long.MAX_VALUE
                        currentTime = (currentTime + 5000).coerceAtMost(duration)
                        audioEngine.seek(currentTime)
                        true
                    }
                    it.key == Key.DirectionUp -> {
                        volume = (volume + 0.05f).coerceAtMost(1f)
                        audioEngine.setVolume(volume)
                        true
                    }
                    it.key == Key.DirectionDown -> {
                        volume = (volume - 0.05f).coerceAtLeast(0f)
                        audioEngine.setVolume(volume)
                        true
                    }
                    it.isCtrlPressed && it.key == Key.O -> { openAudio(); true }
                    it.isCtrlPressed && it.key == Key.L -> { openLyrics(); true }
                    it.isCtrlPressed && it.key == Key.P -> { showPlaylist = !showPlaylist; true }
                    else -> false
                }
            } else false
        },
        menuBar = {
            MenuBar {
                Menu("File") { Item("Settings", onClick = { showSettings = true }) }
                Menu("Tools") { Item("Tap Sync Mode", onClick = { showTapSync = true }) }
            }
        }
    ) {
        val colorScheme = if (settings.theme == AppTheme.Dark)
            darkColorScheme(primary = parseColor(settings.karaokeColor))
        else lightColorScheme(primary = parseColor(settings.karaokeColor))
        MaterialTheme(colorScheme = colorScheme) {
            LaunchedEffect(engineType) {
                val pos = currentTime
                audioEngine.stop()
                audioEngine = when (engineType) {
                    EngineType.JavaFX -> JavaFXAudioEngine()
                    EngineType.TarsosDSP -> TarsosAudioEngine()
                }
                audioEngine.setLevelListener { rms, peak ->
                    rmsLevel = rms
                    peakLevel = peak
                }
                audioEngine.setVolume(volume)
                if (currentTrackIndex in playlist.indices) {
                    val track = playlist[currentTrackIndex]
                    audioEngine.load(File(track.audioPath))
                    audioEngine.setTempo(tempo)
                    audioEngine.setPitch(pitch)
                    audioEngine.seek(pos)
                    if (isPlaying) audioEngine.play()
                }
            }

            LaunchedEffect(currentTrackIndex) {
                waveform = if (currentTrackIndex in playlist.indices) {
                    withContext(Dispatchers.IO) { extractWaveform(File(playlist[currentTrackIndex].audioPath)) }
                } else emptyList()
            }

            LaunchedEffect(volume, audioEngine) { audioEngine.setVolume(volume) }

            LaunchedEffect(isPlaying, currentTrackIndex, audioEngine) {
                while (isPlaying && currentTrackIndex in playlist.indices) {
                    delay(100)
                    currentTime = audioEngine.positionMs()
                    val duration = playlist[currentTrackIndex].durationMs
                    if (currentTime >= duration) {
                        if (currentTrackIndex + 1 < playlist.size) {
                            currentTrackIndex++
                            currentTime = 0L
                            val track = playlist[currentTrackIndex]
                            audioEngine.load(File(track.audioPath))
                            audioEngine.setTempo(tempo)
                            audioEngine.setPitch(pitch)
                            audioEngine.play()
                        } else {
                            isPlaying = false
                            audioEngine.stop()
                        }
                    }
                }
            }

            if (showTapSync) {
                TapSyncPanel(
                    isRunning = tapRunning,
                    tapCount = tapTimes.size,
                    totalLines = lyrics.size,
                    onStart = {
                        tapTimes.clear()
                        currentTime = 0L
                        isPlaying = true
                        tapRunning = true
                    },
                    onStop = {
                        isPlaying = false
                        tapRunning = false
                    },
                    onReset = {
                        tapTimes.clear()
                        currentTime = 0L
                    },
                    onSave = {
                        if (tapTimes.size == lyrics.size) {
                            val updated = lyrics.mapIndexed { index, line ->
                                line.copy(timeMillis = tapTimes[index])
                            }
                            lyrics = updated
                            LrcWriter.write(updated, File("synced.lrc"))
                            showTapSync = false
                        }
                    },
                    onTap = { tapTimes.add(currentTime) },
                    onClose = { showTapSync = false }
                )
            }

            if (showSettings) {
                SettingsDialog(
                    current = settings,
                    onSave = {
                        settings = it
                        engineType = it.defaultEngine
                        volume = it.defaultVolume
                        lyricsFontSize = it.lyricsFontSize
                        SettingsStorage.save(settingsFile, it)
                    },
                    onClose = { showSettings = false }
                )
            }

            Column {
                if (currentTrackIndex in playlist.indices) {
                    val duration = playlist[currentTrackIndex].durationMs
                    WaveformView(
                        waveform = waveform,
                        positionMs = currentTime,
                        durationMs = duration,
                        onSeek = { pos ->
                            currentTime = pos
                            audioEngine.seek(pos)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    )
                }

                Row {
                    if (showPlaylist) {
                        PlaylistPanel(
                            tracks = playlist,
                            currentIndex = currentTrackIndex,
                            onPlay = { index ->
                                currentTrackIndex = index
                                currentTime = 0L
                                val track = playlist[index]
                                audioEngine.load(File(track.audioPath))
                                audioEngine.setTempo(tempo)
                                audioEngine.setPitch(pitch)
                                audioEngine.play()
                                isPlaying = true
                            },
                            onAdd = {
                                playlist.add(it)
                                savePlaylist()
                            },
                            onRemove = { index ->
                                playlist.removeAt(index)
                                savePlaylist()
                                if (currentTrackIndex == index) {
                                    isPlaying = false
                                    currentTime = 0L
                                    currentTrackIndex = -1
                                } else if (index < currentTrackIndex) {
                                    currentTrackIndex--
                                }
                            },
                            onMove = { from, to ->
                                val t = playlist.removeAt(from)
                                playlist.add(to, t)
                                savePlaylist()
                                if (currentTrackIndex == from) currentTrackIndex = to
                                else if (from < currentTrackIndex && to >= currentTrackIndex) currentTrackIndex--
                                else if (from > currentTrackIndex && to <= currentTrackIndex) currentTrackIndex++
                            },
                            onNext = {
                                if (currentTrackIndex + 1 < playlist.size) {
                                    currentTrackIndex++
                                    currentTime = 0L
                                    val track = playlist[currentTrackIndex]
                                    audioEngine.load(File(track.audioPath))
                                    audioEngine.setTempo(tempo)
                                    audioEngine.setPitch(pitch)
                                    if (isPlaying) audioEngine.play()
                                }
                            },
                            onPrev = {
                                if (currentTrackIndex > 0) {
                                    currentTrackIndex--
                                    currentTime = 0L
                                    val track = playlist[currentTrackIndex]
                                    audioEngine.load(File(track.audioPath))
                                    audioEngine.setTempo(tempo)
                                    audioEngine.setPitch(pitch)
                                    if (isPlaying) audioEngine.play()
                                }
                            },
                            modifier = Modifier.width(200.dp),
                        )
                    }

                    LyricView(
                        lyrics = lyrics,
                        currentTime = currentTime,
                        modifier = Modifier.weight(1f),
                        fontSize = lyricsFontSize,
                        onLineClick = { line ->
                            currentTime = line.timeMillis
                            audioEngine.seek(line.timeMillis)
                        }
                    )
                    Column(modifier = Modifier.width(200.dp).padding(8.dp)) {
                        Text("Engine: ${engineType.name}")
                        Row {
                            Button(onClick = { engineType = EngineType.JavaFX }) { Text("JavaFX") }
                            Spacer(Modifier.width(4.dp))
                            Button(onClick = { engineType = EngineType.TarsosDSP }) { Text("TarsosDSP") }
                        }
                        Text("Volume: ${(volume * 100).toInt()}%")
                        Slider(value = volume, onValueChange = {
                            volume = it
                            audioEngine.setVolume(it)
                        })
                        Text("Tempo: ${"%.2f".format(tempo)}x")
                        Slider(
                            value = tempo,
                            onValueChange = {
                                tempo = it
                                audioEngine.setTempo(it)
                            },
                            valueRange = 0.5f..1.5f
                        )
                        Text("Pitch: ${"%.1f".format(pitch)} st")
                        Slider(
                            value = pitch,
                            onValueChange = {
                                pitch = it
                                audioEngine.setPitch(it)
                            },
                            valueRange = -6f..6f
                        )
                        Spacer(Modifier.height(8.dp))
                        LevelMeter(rmsLevel, peakLevel, modifier = Modifier.fillMaxWidth().height(60.dp))
                    }
                }
            }
        }
    }
}
