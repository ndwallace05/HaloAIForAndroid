package xyz.haloai.haloai_android_productivity.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import xyz.haloai.haloai_android_productivity.HaloAI

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val currentProvider = remember { mutableStateOf(HaloAI.getLlmProvider(context)) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select LLM Provider:")
        Row {
            RadioButton(
                selected = currentProvider.value == "openai",
                onClick = {
                    currentProvider.value = "openai"
                    HaloAI.setLlmProvider(context, "openai")
                }
            )
            Text("OpenAI")
        }
        Row {
            RadioButton(
                selected = currentProvider.value == "local",
                onClick = {
                    currentProvider.value = "local"
                    HaloAI.setLlmProvider(context, "local")
                }
            )
            Text("Local LLM")
        }

        if (currentProvider.value == "local") {
            Text(
                text = "Note: Image generation is not supported when using the local LLM.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        ModelManagementScreen()
    }
}
