package ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.karaoke.model.Track
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/** Panel displaying a playlist with basic controls. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistPanel(
    tracks: MutableList<Track>,
    currentIndex: Int,
    onPlay: (Int) -> Unit,
    onAdd: (Track) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf(-1) }

    Column(modifier.padding(8.dp)) {
        Row {
            Button(onClick = {
                newTrack()?.let {
                    onAdd(it)
                    selectedIndex = tracks.lastIndex
                }
            }) { Text("Add") }
            Spacer(Modifier.width(4.dp))
            Button(onClick = {
                if (selectedIndex >= 0) {
                    onRemove(selectedIndex)
                    selectedIndex = -1
                }
            }) { Text("Remove") }
            Spacer(Modifier.width(4.dp))
            Button(onClick = {
                if (selectedIndex > 0) {
                    onMove(selectedIndex, selectedIndex - 1)
                    selectedIndex--
                }
            }) { Text("Up") }
            Spacer(Modifier.width(4.dp))
            Button(onClick = {
                if (selectedIndex >= 0 && selectedIndex < tracks.lastIndex) {
                    onMove(selectedIndex, selectedIndex + 1)
                    selectedIndex++
                }
            }) { Text("Down") }
            Spacer(Modifier.width(4.dp))
            Button(onClick = onPrev, enabled = tracks.isNotEmpty()) { Text("Prev") }
            Spacer(Modifier.width(4.dp))
            Button(onClick = onNext, enabled = tracks.isNotEmpty()) { Text("Next") }
        }

        LazyColumn(modifier.fillMaxHeight().padding(top = 8.dp)) {
            itemsIndexed(tracks) { index, track ->
                val isSelected = index == selectedIndex
                val bg = when {
                    index == currentIndex -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
                Text(
                    "${track.title} - ${track.artist}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .combinedClickable(
                            onClick = { selectedIndex = index },
                            onDoubleClick = { onPlay(index) }
                        )
                        .padding(4.dp)
                )
            }
        }
    }
}

private fun newTrack(): Track? {
    val audio = pickFile("Select audio") ?: return null
    val lyric = pickFile("Select lyrics", required = false) ?: ""
    val title = File(audio).nameWithoutExtension
    return Track(audio, lyric, title, artist = "", durationMs = 30_000)
}

private fun pickFile(title: String, required: Boolean = true): String? {
    val fd = FileDialog(null as Frame?, title, FileDialog.LOAD)
    fd.isVisible = true
    val file = fd.file ?: return if (required) null else ""
    return fd.directory + file
}
