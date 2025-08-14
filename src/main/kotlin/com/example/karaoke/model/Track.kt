package com.example.karaoke.model

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Represents a playable track consisting of an audio file and optional lyric
 * file along with some descriptive metadata.
 */
@Serializable
data class Track(
    val audioPath: String,
    val lyricPath: String,
    val title: String,
    val artist: String,
    val durationMs: Long
)

/** Utility object for persisting playlists as JSON. */
object PlaylistStorage {
    private val json = Json { prettyPrint = true }

    fun load(file: File): List<Track> =
        if (file.exists()) json.decodeFromString(ListSerializer(Track.serializer()), file.readText())
        else emptyList()

    fun save(file: File, tracks: List<Track>) {
        file.writeText(json.encodeToString(ListSerializer(Track.serializer()), tracks))
    }
}
