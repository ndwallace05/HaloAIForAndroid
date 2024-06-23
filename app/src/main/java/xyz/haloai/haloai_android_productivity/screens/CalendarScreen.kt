package xyz.haloai.haloai_android_productivity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import xyz.haloai.haloai_android_productivity.compose_components.CalendarView
import xyz.haloai.haloai_android_productivity.compose_components.SchedulePreview
import xyz.haloai.haloai_android_productivity.compose_components.generateImmutableListOfDates
import xyz.haloai.haloai_android_productivity.ui.theme.HaloAI_Android_ProductivityTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(navController: NavController) {
    // Variable to capture status of calendar, whether or not it is expanded
    val expanded = remember { mutableStateOf(false) }
    val selectedDate = remember { mutableStateOf(Date()) }
    val dragThreshold: Float = 12f.toPx()
    HaloAI_Android_ProductivityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                CalendarView(
                    month = selectedDate.value,
                    date = generateImmutableListOfDates(selectedDate.value, calendarExpanded = expanded
                        .value), //
                    // This param contains all the dates that we want to show. We can use this to expand the view to show the entire month.
                    displayNext = true,
                    displayPrev = true,
                    onClickNext = {
                        // Set date as the first day of the next month, and expand the calendar
                        selectedDate.value = Calendar.getInstance().apply {
                            time = selectedDate.value
                            add(Calendar.MONTH, 1)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }.time
                        expanded.value = true
                    },
                    onClickPrev = {
                        // Set date as the first day of the previous month, and expand the calendar
                        selectedDate.value = Calendar.getInstance().apply {
                            time = selectedDate.value
                            add(Calendar.MONTH, -1)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }.time
                        expanded.value = true
                    },
                    onClick = {
                        // Set the selected date
                        selectedDate.value = it
                    },
                    startFromSunday = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                if (dragAmount < -dragThreshold) {
                                    // Dragged up
                                    expanded.value = false
                                } else if (dragAmount > dragThreshold) {
                                    // Dragged down
                                    expanded.value = true
                                }
                            }
                        }
                )
                // A bar (small rectangle, rounded corners) which when clicked will expand the calendar to show the entire month
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(colorScheme.primaryContainer)
                        .clickable { expanded.value = !expanded.value }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                if (dragAmount < 0) {
                                    // Dragged up
                                    expanded.value = false
                                } else {
                                    // Dragged down
                                    expanded.value = true
                                }
                            }
                        }
                ) {}
                TasksView()
                // EventsView(selectedDate = selectedDate.value)
                SchedulePreview()
            }
        }
    }
}

@Composable
fun EventsView(selectedDate: Date? = null) {
    val times = (0..23).flatMap { hour ->
        listOf(
            String.format(Locale.getDefault(), "%02d:00", hour),
            String.format(Locale.getDefault(), "%02d:30", hour)
        )
    }

    val now = Date()
    // If it is today, find the closest time index, else set it to 0
    if (selectedDate != null && SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate) ==
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)) {
        val closestTimeIndex = times.indexOfFirst {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = formatter.format(now)
            it >= currentTime
        }
    } else {
        0
    }

    LazyColumn {

    }
}

@Composable
fun TasksView() {
    // TODO("Not yet implemented")
}

@Composable
fun Float.toPx(): Float {
    val density = LocalDensity.current
    return with(density) { this@toPx.toDp().toPx() }
}

