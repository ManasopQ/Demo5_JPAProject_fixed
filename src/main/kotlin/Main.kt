import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.LyricLine
import ui.LyricView

/**
 * Entry point showcasing the [LyricView]. It advances [currentTime] every
 * few milliseconds to emulate audio playback.
 */
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Lyrics") {
        MaterialTheme {
            val lyrics = remember {
                listOf(
                    LyricLine(0L, "Line 1"),
                    LyricLine(3000L, "Line 2"),
                    LyricLine(6000L, "Line 3"),
                    LyricLine(9000L, "Line 4")
                )
            }
            var currentTime by remember { mutableStateOf(0L) }

            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
                scope.launch {
                    while (true) {
                        delay(100)
                        currentTime += 100
                    }
                }
            }

            LyricView(lyrics = lyrics, currentTime = currentTime)
        }
    }
}
