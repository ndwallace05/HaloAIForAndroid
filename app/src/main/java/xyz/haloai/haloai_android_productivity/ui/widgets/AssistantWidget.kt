package xyz.haloai.haloai_android_productivity.ui.widgets

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import xyz.haloai.haloai_android_productivity.MainActivity
import xyz.haloai.haloai_android_productivity.R

class AssistantWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AssistantWidget()
}

class AssistantWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            AssistantWidgetLayout(context)
        }
    }
}

@Composable
fun AssistantWidgetLayout(context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                .cornerRadius(8.dp)
                .padding(horizontal = 12.dp, vertical = 12.dp)

        ) {
            Image(
                provider = ImageProvider(R.drawable.haloai_logo),
                contentDescription = null,
                modifier = GlanceModifier.size(24.dp),
                colorFilter = ColorFilter.tint(ColorProvider(MaterialTheme.colorScheme.onSurface))
            )
            val assistantScreenParameterKey = ActionParameters.Key<String>("defaultDestination")
            Text(modifier =
            GlanceModifier.clickable(onClick =
                actionStartActivity<MainActivity>(
                    parameters = actionParametersOf( assistantScreenParameterKey to "Screens.Assistant")
                )).defaultWeight(),
            text = ""
            )
            Image(
                provider = ImageProvider(R.drawable.baseline_mic_24),
                contentDescription = null,
                modifier = GlanceModifier.size(24.dp),
                colorFilter = ColorFilter.tint(ColorProvider(MaterialTheme.colorScheme.onSurface))
            )
        }
    }
}
