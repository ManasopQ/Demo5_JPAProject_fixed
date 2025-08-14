import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ui.LyricLine
import ui.LyricView
import ui.TapSyncPanel
import ui.PlaylistPanel
import com.example.karaoke.lyrics.LrcWriter
import com.example.karaoke.model.PlaylistStorage
import com.example.karaoke.model.Track
import java.io.File

/**
 * Entry point showcasing the [LyricView]. It advances [currentTime] every
 * few milliseconds to emulate audio playback.
 */
fun main() = application {
    var showTapSync by remember { mutableStateOf(false) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Lyrics",
        menuBar = {
            MenuBar {
                Menu("Tools") {
                    Item("Tap Sync Mode", onClick = { showTapSync = true })
                }
            }
        }
    ) {
        MaterialTheme {
            val playlistFile = File("playlist.json")
            val playlist = remember { mutableStateListOf<Track>() }
            LaunchedEffect(Unit) { playlist.addAll(PlaylistStorage.load(playlistFile)) }
            fun savePlaylist() = PlaylistStorage.save(playlistFile, playlist)

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
            val tapTimes = remember { mutableStateListOf<Long>() }
            var tapRunning by remember { mutableStateOf(false) }

            LaunchedEffect(isPlaying, currentTrackIndex) {
                while (isPlaying && currentTrackIndex in playlist.indices) {
                    delay(100)
                    currentTime += 100
                    val duration = playlist[currentTrackIndex].durationMs
                    if (currentTime >= duration) {
                        if (currentTrackIndex + 1 < playlist.size) {
                            currentTrackIndex++
                            currentTime = 0L
                        } else {
                            isPlaying = false
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

            Row {
                PlaylistPanel(
                    tracks = playlist,
                    currentIndex = currentTrackIndex,
                    onPlay = { index ->
                        currentTrackIndex = index
                        currentTime = 0L
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
                        }
                    },
                    onPrev = {
                        if (currentTrackIndex > 0) {
                            currentTrackIndex--
                            currentTime = 0L
                        }
                    },
                    modifier = Modifier.width(200.dp)
                )

                LyricView(
                    lyrics = lyrics,
                    currentTime = currentTime,
                    modifier = Modifier.weight(1f),
                    onLineClick = { line -> currentTime = line.timeMillis }
                )
            }
        }
    }
}
