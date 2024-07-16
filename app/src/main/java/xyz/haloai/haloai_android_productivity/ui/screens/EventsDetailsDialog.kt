package xyz.haloai.haloai_android_productivity.ui.screens

import android.text.util.Linkify
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.text.util.LinkifyCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.data.local.entities.ScheduleEntry
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventsDetailsDialog(eventId: Long, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val scheduleDbViewModel: ScheduleDbViewModel = koinInject { parametersOf(context) }
    var coroutineScope: CoroutineScope = rememberCoroutineScope()
    var eventDbEntry: ScheduleEntry? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            eventDbEntry = scheduleDbViewModel.getEventById(eventId)
        }
    }


    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (eventDbEntry == null) {
                CircularProgressIndicator()
            }
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.secondary,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = eventDbEntry!!.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    item {
                        val startDateTime: Date? = eventDbEntry!!.startTime
                        val endDateTime: Date? = eventDbEntry!!.endTime
                        // Date and time of the event formatted as "Friday, Jun 21 • 7:00 – 11:59 PM"
                        var dateFormattedStr = "Unknown"
                        if (startDateTime != null && endDateTime != null) {
                            // Define the date and time format
                            val dayFormat =
                                SimpleDateFormat("EEEE", Locale.getDefault()) // For day of the week
                            val dateFormat =
                                SimpleDateFormat("MMM d", Locale.getDefault()) // For date
                            val timeFormat =
                                SimpleDateFormat("h:mm a", Locale.getDefault()) // For time

                            // Format the start and end times
                            val day = dayFormat.format(startDateTime)
                            val date = dateFormat.format(startDateTime)
                            val startTime = timeFormat.format(startDateTime)
                            val endTime = timeFormat.format(endDateTime)

                            // Combine them into the final format
                            dateFormattedStr = "$day, $date • $startTime – $endTime"
                        }
                        LinkifyText(
                            text = dateFormattedStr,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        var descriptionText = "-"
                        if (eventDbEntry!!.description != null) {
                            descriptionText = eventDbEntry!!.description!!
                        }
                        EventDetailRow(
                            icon = Icons.Default.Info,
                            content = descriptionText
                        )
                    }
                    item {
                        var locationText = "-"
                        if (eventDbEntry!!.location != null) {
                            locationText = eventDbEntry!!.location!!
                        }
                        EventDetailRow(
                            icon = Icons.Default.LocationOn,
                            content = locationText
                        )
                    }
                    item {
                        EventDetailRow(
                            icon = Icons.Default.Notifications,
                            content = "15 minutes before"
                        )
                    }
                    item {
                        EventDetailRow(
                            icon = Icons.Default.Email,
                            content = eventDbEntry!!.sourceEmailId
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun EventDetailRow(icon: ImageVector, content: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        LinkifyText(text = content, fontSize = 16.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
    }
}

@Composable
fun LinkifyText(text: String, modifier: Modifier, fontSize: TextUnit, textAlign: TextAlign) {
    val context = LocalContext.current
    val density = LocalDensity.current

    AndroidView(factory = {
        TextView(context).apply {
            setText(text)
            autoLinkMask = Linkify.ALL
            LinkifyCompat.addLinks(this, Linkify.ALL)
            setTextIsSelectable(true) // Optional: to make the text selectable
            setTextSize(fontSize.value)
            textAlignment = when (textAlign) {
                TextAlign.Left -> TextView.TEXT_ALIGNMENT_TEXT_START
                TextAlign.Right -> TextView.TEXT_ALIGNMENT_TEXT_END
                TextAlign.Center -> TextView.TEXT_ALIGNMENT_CENTER
                TextAlign.Justify -> TextView.TEXT_ALIGNMENT_TEXT_START // Justify is not directly supported
                TextAlign.Start -> TextView.TEXT_ALIGNMENT_VIEW_START
                TextAlign.End -> TextView.TEXT_ALIGNMENT_VIEW_END
                else -> TextView.TEXT_ALIGNMENT_INHERIT
            }
        }
    },
        modifier = modifier
    )
}