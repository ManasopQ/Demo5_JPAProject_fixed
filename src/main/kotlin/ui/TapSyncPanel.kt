package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun TapSyncPanel(
    isRunning: Boolean,
    tapCount: Int,
    totalLines: Int,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onTap: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(onCloseRequest = onClose) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("$tapCount / $totalLines taps")
                Spacer(Modifier.height(8.dp))
                Button(onClick = onTap, enabled = isRunning) { Text("Tap") }
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = if (isRunning) onStop else onStart) {
                        Text(if (isRunning) "Stop" else "Start")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onReset) { Text("Reset") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onSave, enabled = tapCount == totalLines) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
