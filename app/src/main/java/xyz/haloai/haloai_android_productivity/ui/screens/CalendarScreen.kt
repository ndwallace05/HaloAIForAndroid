package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.data.ui.compose_components.CalendarView
import xyz.haloai.haloai_android_productivity.data.ui.compose_components.EventDataForUI
import xyz.haloai.haloai_android_productivity.data.ui.compose_components.Schedule
import xyz.haloai.haloai_android_productivity.data.ui.compose_components.generateImmutableListOfDates
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme
import xyz.haloai.haloai_android_productivity.misc.normalizeToUTC
import xyz.haloai.haloai_android_productivity.ui.screens.EventsDetailsDialog
import xyz.haloai.haloai_android_productivity.ui.viewmodel.GmailViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun CalendarScreen(navController: NavController) {
    // Variable to capture status of calendar, whether or not it is expanded
    val expanded = remember { mutableStateOf(false) }
    val selectedDate = remember { mutableStateOf(Date()) }
    val dragThreshold: Float = 12f.toPx()
    var dragAmountX by remember { mutableFloatStateOf(0f) }
    var dragAmountY by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val gmailViewModel: GmailViewModel = koinViewModel() // Refresh the
    // view model
    val scheduleDbViewModel: ScheduleDbViewModel = koinViewModel { parametersOf(context, true) } //
    // Refresh the view model
    val listOfEventsToDisplay = remember { mutableStateListOf<EventDataForUI>() }
    var selectedEventId: Long? by remember {
        mutableStateOf(null)
    }
    // Function to handle the click event and receive data
    val onEventClick: (Long) -> Unit = { eventId ->
        selectedEventId = eventId
    }
    val onDismiss: () -> Unit = {
        selectedEventId = null
    }

    LaunchedEffect (selectedDate.value) {
        coroutineScope.launch {
            // scheduleDbViewModel.updateScheduleDb(context)
            var timeForSelectedDate = Calendar.getInstance()
            timeForSelectedDate.time = selectedDate.value
            var selectedDateStartOfDay = timeForSelectedDate.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            var selectedDateEndOfDay = timeForSelectedDate.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time

            // UTC Normalized time (Date) for the start of the selected date
            val utcStartOfDay = normalizeToUTC(selectedDateStartOfDay)
            val utcEndOfDay = normalizeToUTC(selectedDateEndOfDay)

            val allEventsToDisplay = scheduleDbViewModel.getEventsBetween(utcStartOfDay, utcEndOfDay)
            listOfEventsToDisplay.clear()
            allEventsToDisplay.forEach {
                var startAsLocalDateTime = it.startTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                var endAsLocalDateTime = it.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                listOfEventsToDisplay.add(EventDataForUI(
                    name = it.title,
                    color = Color(0xFF6DD3CE), // Replace with colors specific to the calendar
                    start = startAsLocalDateTime,
                    end = endAsLocalDateTime,
                    description = it.description,
                    id = it.id
                ))
            }
        }
    }


    HaloAI_Android_ProductivityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
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
                            detectDragGestures(
                                onDragStart = {
                                    dragAmountX = 0f
                                    dragAmountY = 0f
                                },
                                onDragEnd = {
                                    if (abs(dragAmountX) > abs(dragAmountY)) {
                                        when {
                                            dragAmountX < -dragThreshold -> {
                                                // Dragged left
                                                if (expanded.value) {
                                                    selectedDate.value = Calendar
                                                        .getInstance()
                                                        .apply {
                                                            time = selectedDate.value
                                                            add(Calendar.MONTH, 1)
                                                        }.time
                                                } else {
                                                    selectedDate.value = Calendar
                                                        .getInstance()
                                                        .apply {
                                                            time = selectedDate.value
                                                            add(Calendar.WEEK_OF_YEAR, 1)
                                                        }.time
                                                }
                                            }

                                            dragAmountX > dragThreshold -> {
                                                // Dragged right
                                                if (expanded.value) {
                                                    selectedDate.value = Calendar
                                                        .getInstance()
                                                        .apply {
                                                            time = selectedDate.value
                                                            add(Calendar.MONTH, -1)
                                                        }.time
                                                } else {
                                                    selectedDate.value = Calendar
                                                        .getInstance()
                                                        .apply {
                                                            time = selectedDate.value
                                                            add(Calendar.WEEK_OF_YEAR, -1)
                                                        }.time
                                                }
                                            }
                                        }
                                    } else {
                                        when {
                                            dragAmountY < -dragThreshold -> {
                                                // Dragged up
                                                expanded.value = false
                                            }

                                            dragAmountY > dragThreshold -> {
                                                // Dragged down
                                                expanded.value = true
                                            }
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAmountX += dragAmount.x
                                    dragAmountY += dragAmount.y
                                }
                            )
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
                var dateAsLocalDate = selectedDate.value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                // Get start of selected date as a LocalTime instance
                var startTimeAsLocalTime = dateAsLocalDate.atStartOfDay().toLocalTime()
                // Get end of selected date as a LocalTime instance
                var endTimeAsLocalTime = dateAsLocalDate.atTime(23, 59).toLocalTime()

                Schedule(
                    events = listOfEventsToDisplay,
                    minDate = dateAsLocalDate,
                    maxDate = dateAsLocalDate,
                    minTime = startTimeAsLocalTime,
                    maxTime = endTimeAsLocalTime,
                    onEventClickFunction = onEventClick
                )
            }
            if (selectedEventId != null) {
                // Show the dialog with the event details
                EventsDetailsDialog(eventId = selectedEventId!!, onDismissRequest = onDismiss)
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

