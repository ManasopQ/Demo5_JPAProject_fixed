package ui

import androidx.compose.animation.core.withFrameNanos
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Represents a line of lyric with its start time in milliseconds.
 */
data class LyricLine(val timeMillis: Long, val text: String)

/**
 * Displays [lyrics] and keeps the currently active line centered while
 * highlighting it with a soft background. A thin progress bar under the active
 * line reflects the elapsed time until the next line.
 */
@Composable
fun LyricView(
    lyrics: List<LyricLine>,
    currentTime: Long,
    modifier: Modifier = Modifier,
    onLineClick: (LyricLine) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val currentIndex = remember(currentTime) {
        lyrics.indexOfLast { currentTime >= it.timeMillis }
    }.coerceAtLeast(0)

    // Smoothly scroll so that the active line stays centered.
    LaunchedEffect(currentIndex) {
        if (lyrics.isEmpty()) return@LaunchedEffect
        listState.animateScrollToItem(currentIndex)
        withFrameNanos { }
        val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentIndex }
        val viewportCenter = listState.layoutInfo.viewportSize.height / 2
        val offset = itemInfo?.let { viewportCenter - it.size / 2 } ?: viewportCenter
        listState.animateScrollToItem(currentIndex, offset)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(lyrics) { index, line ->
            val next = lyrics.getOrNull(index + 1)?.timeMillis ?: Long.MAX_VALUE
            val progress = ((currentTime - line.timeMillis).coerceAtLeast(0).toFloat() /
                (next - line.timeMillis)).coerceIn(0f, 1f)
            val isActive = index == currentIndex

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLineClick(line) }
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else Color.Transparent
                    )
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = line.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onBackground
                )
                if (isActive) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
