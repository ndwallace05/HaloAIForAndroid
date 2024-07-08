package xyz.haloai.haloai_android_productivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.NavBarsWithContent
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.Screens

class MainActivity() : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var defaultDestination: Screens = Screens.Home
        val destinationFromIntent = intent.getStringExtra("defaultDestination")
        if (destinationFromIntent != null) {
            if (destinationFromIntent == "Screens.Assistant") {
                defaultDestination = Screens.Assistant
            }
        }

        enableEdgeToEdge()
        setContent {
            HaloAI_Android_ProductivityTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NavBarsWithContent(defaultDestination)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HaloAI_Android_ProductivityTheme {
        Greeting("Android")
    }
}