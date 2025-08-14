import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay
import ui.LyricLine
import ui.LyricView
import ui.TapSyncPanel
import com.example.karaoke.lyrics.LrcWriter
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
            var isPlaying by remember { mutableStateOf(true) }
            val tapTimes = remember { mutableStateListOf<Long>() }
            var tapRunning by remember { mutableStateOf(false) }

            LaunchedEffect(isPlaying) {
                while (isPlaying) {
                    delay(100)
                    currentTime += 100
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

            LyricView(
                lyrics = lyrics,
                currentTime = currentTime,
                onLineClick = { line -> currentTime = line.timeMillis }
            )
        }
    }
}
