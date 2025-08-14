package com.example.karaoke.lyrics

import ui.LyricLine
import java.io.File

/** Simple parser for LRC lyric files. */
object LrcReader {
    private val regex = "\\[(\\d+):(\\d+\\.\\d+)\\]".toRegex()

    fun read(file: File): List<LyricLine> {
        if (!file.exists()) return emptyList()
        return file.readLines().mapNotNull { line ->
            val match = regex.find(line) ?: return@mapNotNull null
            val min = match.groupValues[1].toLong()
            val sec = match.groupValues[2].toDouble()
            val time = min * 60_000 + (sec * 1000).toLong()
            val text = line.substring(match.range.last + 1).trim()
            LyricLine(time, text)
        }.sortedBy { it.timeMillis }
    }
}
