package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.karaoke.audio.EngineType
import com.example.karaoke.model.Settings
import com.example.karaoke.model.Theme

@Composable
fun SettingsDialog(
    current: Settings,
    onSave: (Settings) -> Unit,
    onClose: () -> Unit
) {
    var theme by remember { mutableStateOf(current.theme) }
    var engine by remember { mutableStateOf(current.defaultEngine) }
    var volume by remember { mutableStateOf(current.defaultVolume) }
    var fontSize by remember { mutableStateOf(current.lyricsFontSize.toFloat()) }
    var color by remember { mutableStateOf(current.karaokeColor) }

    Dialog(onCloseRequest = onClose) {
        Surface {
            Column(Modifier.padding(16.dp)) {
                Text("Theme")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = theme == Theme.Light, onClick = { theme = Theme.Light })
                    Text("Light")
                    Spacer(Modifier.width(8.dp))
                    RadioButton(selected = theme == Theme.Dark, onClick = { theme = Theme.Dark })
                    Text("Dark")
                }

                Spacer(Modifier.height(8.dp))
                Text("Default Engine")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EngineType.values().forEach { et ->
                        RadioButton(selected = engine == et, onClick = { engine = et })
                        Text(et.name)
                        Spacer(Modifier.width(8.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Default Volume: ${(volume * 100).toInt()}%")
                Slider(value = volume, onValueChange = { volume = it })

                Spacer(Modifier.height(8.dp))
                Text("Lyrics Font Size: ${fontSize.toInt()}")
                Slider(value = fontSize, onValueChange = { fontSize = it }, valueRange = 10f..48f)

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Karaoke Color (#RRGGBB)") }
                )

                Spacer(Modifier.height(16.dp))
                Row {
                    Button(onClick = {
                        onSave(
                            Settings(theme, engine, volume, fontSize.toInt(), color)
                        )
                        onClose()
                    }) { Text("Save") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onClose) { Text("Cancel") }
                }
            }
        }
    }
}
