package com.example.karaoke.lyrics

import ui.LyricLine
import java.io.File

/** Utility for writing lyric lines to an LRC file. */
object LrcWriter {
    /** Writes [lines] to [file] in simple `.lrc` format. */
    fun write(lines: List<LyricLine>, file: File) {
        val content = buildString {
            lines.forEach { line ->
                append(formatTimestamp(line.timeMillis))
                append(line.text)
                append('\n')
            }
        }
        file.writeText(content)
    }

    private fun formatTimestamp(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hundredths = (ms % 1000) / 10
        return "[%02d:%02d.%02d]".format(minutes, seconds, hundredths)
    }
}
