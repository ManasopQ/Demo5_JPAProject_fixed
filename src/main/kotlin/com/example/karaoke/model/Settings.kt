package com.example.karaoke.model

import com.example.karaoke.audio.EngineType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
enum class Theme { Light, Dark }

@Serializable
data class Settings(
    val theme: Theme = Theme.Light,
    val defaultEngine: EngineType = EngineType.JavaFX,
    val defaultVolume: Float = 1f,
    val lyricsFontSize: Int = 16,
    val karaokeColor: String = "#6200EE"
)

object SettingsStorage {
    private val json = Json { prettyPrint = true }

    fun load(file: File): Settings =
        if (file.exists()) json.decodeFromString(Settings.serializer(), file.readText())
        else Settings()

    fun save(file: File, settings: Settings) {
        file.writeText(json.encodeToString(Settings.serializer(), settings))
    }
}
