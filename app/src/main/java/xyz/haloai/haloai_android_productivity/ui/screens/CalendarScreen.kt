package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.data.ui.compose_components.CalendarView
import xyz.haloai.haloai_android_productivity.data.ui.compose_components.EventDataForUI
import xyz.haloai.haloai_android_productivity.data.ui.compose_components.Schedule
import xyz.haloai.haloai_android_productivity.data.ui.compose_components.generateImmutableListOfDates
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme
import xyz.haloai.haloai_android_productivity.misc.normalizeToUTC
import xyz.haloai.haloai_android_productivity.ui.screens.EventsDetailsDialog
import xyz.haloai.haloai_android_productivity.ui.screens.TaskDetailsDialog
import xyz.haloai.haloai_android_productivity.ui.viewmodel.GmailViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
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
    val scheduleDbViewModel: ScheduleDbViewModel = koinViewModel { parametersOf(context) } //
    // Refresh the view model
    val listOfEventsToDisplay = remember { mutableStateListOf<EventDataForUI>() }
    val listOfConfirmedTasks = remember { mutableStateListOf<TaskDataForCalendarUI>() }
    var selectedEventId: Long? by remember {
        mutableStateOf(null)
    }
    var selectedTaskId: Long? by remember {
        mutableStateOf(null)
    }
    // Function to handle the click event and receive data
    val onEventClick: (Long) -> Unit = { eventId ->
        selectedEventId = eventId
    }
    val onTaskClick: (Long) -> Unit = { taskId ->
        selectedTaskId = taskId
    }
    val onEventDismiss: () -> Unit = {
        selectedEventId = null
    }
    val onTaskDismiss: () -> Unit = {
        selectedTaskId = null
    }
    var suggestedTasks = remember {
        mutableStateListOf<TaskDataForCalendarUI>()
    }
    val onSuggestedTaskAddClick: (Long) -> Unit = { taskId ->
        // Change type of task to SCHEUDLED_TASK, and set date to selected date
        coroutineScope.launch {
            var startDate = selectedDate.value
            var endDate = selectedDate.value
            var newId = scheduleDbViewModel.insertOrUpdate(
                context = context,
                id = taskId,
                type = enumEventType.SCHEDULED_TASK,
                startTime = startDate,
                endTime = endDate
            )

        }
    }


    LaunchedEffect (selectedDate.value) { // Events
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
                // If end time is 12:00 AM, set it to 11:59 PM
                if (endAsLocalDateTime.toLocalTime().hour == 0 && endAsLocalDateTime.toLocalTime().minute == 0) {
                    endAsLocalDateTime = endAsLocalDateTime.minusSeconds(1)
                }
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

    LaunchedEffect (selectedDate.value) { // Tasks
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

            val allTasksToDisplay = scheduleDbViewModel.getTasksBetween(utcStartOfDay, utcEndOfDay)
            listOfConfirmedTasks.clear()
            allTasksToDisplay.forEach {
                var startAsLocalDateTime = it.startTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                var endAsLocalDateTime = it.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                listOfConfirmedTasks.add(
                    TaskDataForCalendarUI(
                    taskName = it.title,
                    taskDescription = if (it.description != null) it.description!! else "",
                    isChecked = it.isCompleted,
                    taskId = it.id,
                    isScheduled = true
                )
                )
            }
        }
    }

    LaunchedEffect(selectedDate.value) {
        suggestedTasks.clear()
        // Get suggested tasks for the selected date
        scheduleDbViewModel.getSuggestedTasksForDay(context, selectedDate.value)
    }


    HaloAI_Android_ProductivityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Calendar header for date selection
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
                Spacer(modifier = Modifier.height(10.dp))
                TaskListForCalendar(
                    coroutineScope = coroutineScope,
                    scheduleDbViewModel = scheduleDbViewModel,
                    confirmedTasks = listOfConfirmedTasks,
                    onClick = onTaskClick,
                    onSuggestedTaskAddClick = onSuggestedTaskAddClick
                )
                Spacer(modifier = Modifier.height(10.dp))
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
                EventsDetailsDialog(eventId = selectedEventId!!, onDismissRequest = onEventDismiss)
            }
            if (selectedTaskId != null) {
                // Show the dialog with the task details
                TaskDetailsDialog(taskId = selectedTaskId!!, onDismissRequest = onTaskDismiss)
            }
        }
    }
}

@Composable
fun TaskListForCalendar(coroutineScope: CoroutineScope, scheduleDbViewModel: ScheduleDbViewModel,
                        confirmedTasks : List<TaskDataForCalendarUI>, onClick: (Long) -> Unit = {},
                        onSuggestedTaskAddClick: (Long) -> Unit = {}) {
    val isCheckedStateForConfirmedTasks = remember { mutableStateListOf<Boolean>() }
    for (task in confirmedTasks) {
        isCheckedStateForConfirmedTasks.add(task.isChecked)
    }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .border(
                1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), shape =
                RoundedCornerShape(20.dp)
            )
            .padding(8.dp)
        // Add a border to the column
    ) {
        for (task in confirmedTasks) {
            ConfirmedTask(
                task = task,
                checked = isCheckedStateForConfirmedTasks[confirmedTasks.indexOf(task)],
                onCheckedChange = {
                    isChecked -> isCheckedStateForConfirmedTasks[confirmedTasks.indexOf(task)] = isChecked
                    task.isChecked = isChecked
                    coroutineScope.launch {
                        scheduleDbViewModel.updateCompletionStatus(task.taskId, isChecked)
                    }
                },
                onClick = onClick
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        /*ConfirmedTask(
            taskName = "Sample Task 1",
            checked = true,
            onCheckedChange = {}
        )
        Spacer(modifier = Modifier.height(4.dp))
        ConfirmedTask(
            taskName = "Sample Task 2",
            checked = false,
            onCheckedChange = {}
        )*/
        // Spacer(modifier = Modifier.height(4.dp))

        SuggestedTask(
            task = TaskDataForCalendarUI(
                taskName = "Suggested Task 1",
                taskDescription = "This is a suggested task",
                isChecked = false,
                taskId = 1,
                isScheduled = false
            ),
            onClick = onClick
        )
        Spacer(modifier = Modifier.height(4.dp))
        SeeMoreButton(onClick = {})
    }
}

@Composable
fun ConfirmedTask(task: TaskDataForCalendarUI, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
                  onClick: (Long) -> Unit) {
    val backgroundColor = if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(20.dp))
            .padding(4.dp) // Adjust the padding to a minimum
            .clickable { onClick(task.taskId) }
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(MaterialTheme.colorScheme.onPrimaryContainer),
            modifier = Modifier.size(20.dp) // Reduce size to tightly fit
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = task.taskName,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodySmall,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.padding(vertical = 4.dp) // Minimize vertical padding
        )

        if (!checked) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription =
                "Unschedule Task",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun SuggestedTask(task: TaskDataForCalendarUI, onClick: (Long) -> Unit, onAddClick: (Long) -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), shape = RoundedCornerShape
                    (20.dp)
            )
            .padding(4.dp)
            .clickable { onClick(task.taskId) }
    ) {
        var onAddClick = {

        }
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(onClick = { onAddClick(task.taskId) }, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task", tint =
            MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = task.taskName,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SeeMoreButton(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), shape = RoundedCornerShape
                    (20.dp)
            )
            .padding(4.dp)
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "See More", tint =
            MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "See more",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun Float.toPx(): Float {
    val density = LocalDensity.current
    return with(density) { this@toPx.toDp().toPx() }
}

data class TaskDataForCalendarUI(
    val taskName: String,
    val taskDescription: String,
    var isChecked: Boolean,
    val taskId: Long,
    val isScheduled: Boolean
)

