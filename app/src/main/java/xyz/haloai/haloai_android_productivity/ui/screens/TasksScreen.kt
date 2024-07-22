package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme
import xyz.haloai.haloai_android_productivity.ui.screens.TaskDetailsDialog
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

@Composable
fun TasksScreen(navController: NavController) {
    val tasks = remember { mutableStateListOf<TaskDataForUi>(
        TaskDataForUi(
            id = 0,
            taskName = "Fetching...",
            taskDescription = "...",
            isChecked = false,
            startDate = Date()
        )
    ) }
    val context = LocalContext.current
    val scheduleDbViewModel: ScheduleDbViewModel = koinViewModel { parametersOf(context) }
    val coroutineScope = rememberCoroutineScope()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    var showSeeMore = remember { mutableStateOf(false) }
    val extraTasks = remember { mutableStateListOf<TaskDataForUi>() }
    var selectedTaskId = remember { mutableStateOf<Long?>(null) }

    LaunchedEffect (Unit) {
        coroutineScope.launch {
            tasks.clear()
            val allUnscheduledTasks = scheduleDbViewModel.getAllUnscheduledTasks()
            val allScheduledTasks = scheduleDbViewModel.getAllScheduledTasks()
            for (taskDetails in allUnscheduledTasks.subList(0, allUnscheduledTasks.size.coerceAtMost(10)))
            {
                tasks.add(
                    TaskDataForUi(
                        id = taskDetails.id,
                        taskName = taskDetails.title,
                        taskDescription = taskDetails.description ?: "",
                        isChecked = taskDetails.isCompleted,
                        startDate = null
                    )
                )
            }

            if (tasks.size >= 10)
            {
                showSeeMore.value = true
                extraTasks.clear()
                for (taskDetails in allUnscheduledTasks.subList(10, allUnscheduledTasks.size))
                {
                    extraTasks.add(
                        TaskDataForUi(
                            id = taskDetails.id,
                            taskName = taskDetails.title,
                            taskDescription = taskDetails.description ?: "",
                            isChecked = taskDetails.isCompleted,
                            startDate = null
                        )
                    )
                }
                for (taskDetails in allScheduledTasks)
                {
                    extraTasks.add(
                        TaskDataForUi(
                            id = taskDetails.id,
                            taskName = taskDetails.title,
                            taskDescription = taskDetails.description ?: "",
                            isChecked = taskDetails.isCompleted,
                            startDate = taskDetails.startTime
                        )
                    )
                }
            }

            else {
                val numExistingTasksToShow = tasks.size
                for (taskDetails in allScheduledTasks.subList(0, allScheduledTasks.size.coerceAtMost(10 - numExistingTasksToShow))) {
                    var descriptionText = taskDetails.description ?: ""
                    if (taskDetails.startTime != null) {
                        descriptionText =
                            " (${dateFormatter.format(taskDetails.startTime!!)})" + descriptionText
                    }
                    tasks.add(
                        TaskDataForUi(
                            id = taskDetails.id,
                            taskName = taskDetails.title,
                            taskDescription = descriptionText,
                            isChecked = taskDetails.isCompleted,
                            startDate = taskDetails.startTime
                        )
                    )
                }

                if (allScheduledTasks.size > 10 - numExistingTasksToShow) {
                    showSeeMore.value = true
                    extraTasks.clear()
                    for (taskDetails in allScheduledTasks.subList(10 - tasks.size, allScheduledTasks.size)) {
                        var descriptionText = taskDetails.description ?: ""
                        if (taskDetails.startTime != null) {
                            descriptionText =
                                " (${dateFormatter.format(taskDetails.startTime!!)})" + descriptionText
                        }
                        extraTasks.add(
                            TaskDataForUi(
                                id = taskDetails.id,
                                taskName = taskDetails.title,
                                taskDescription = descriptionText,
                                isChecked = taskDetails.isCompleted,
                                startDate = taskDetails.startTime
                            )
                        )
                    }
                }
            }
        }
    }

    HaloAI_Android_ProductivityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        TaskList(
                            tasks = tasks,
                            onDelete = { task ->
                                tasks.remove(task)
                            },
                            showSeeMore = showSeeMore.value,
                            onShowSeeMoreClick = {
                                tasks.addAll(extraTasks)
                                extraTasks.clear()
                                showSeeMore.value = false
                            },
                            onClick = {
                                selectedTaskId.value = it
                            },
                            context = context
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }

                }

                SearchBarForTasks(modifier = Modifier.align(Alignment.BottomCenter))
            }

            if (selectedTaskId.value != null) {
                TaskDetailsDialog(
                    taskId = selectedTaskId.value!!,
                    onDismissRequest =
                    {
                        selectedTaskId.value = null
                    }
                )
            }
        }
    }
}

@Composable
fun TaskList(tasks: List<TaskDataForUi>, onDelete: (TaskDataForUi) -> Unit = {}, showSeeMore: Boolean = false, onShowSeeMoreClick: () -> Unit = {}, onClick: (Long) -> Unit, context: Context) {
    Column {
        for(task in tasks) {
            TaskItem(task = task, onDelete = onDelete, modifier = Modifier.clickable { onClick(task.id) }, context = context)
        }
        if (showSeeMore) {
            Button(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                onClick = {
                    // Add extra tasks to the list
                    onShowSeeMoreClick()
                })
            {
                Text("See more...", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer,)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(task: TaskDataForUi, onDelete: (TaskDataForUi) -> Unit = {}, modifier: Modifier, context: Context) {
    var isChecked by remember { mutableStateOf(task.isChecked) }
    val scheduleDbViewModel: ScheduleDbViewModel = koinViewModel { parametersOf(context) }
    val coroutineScope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = task.startDate?.time ?: System.currentTimeMillis())

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    var selectedDateMillis = datePickerState.selectedDateMillis ?: return@TextButton
                    // This is in UTC, adjust to local time
                    selectedDateMillis -= TimeZone.getDefault().getOffset(selectedDateMillis)
                    // Set time to 8 AM
                    val calendar = java.util.Calendar.getInstance().apply {
                        timeZone = TimeZone.getDefault()
                        timeInMillis = selectedDateMillis
                        set(java.util.Calendar.HOUR_OF_DAY, 8)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    selectedDateMillis = calendar.timeInMillis
                    coroutineScope.launch {
                        scheduleDbViewModel.updateScheduleEntryWithOnlyGivenFields(
                            startTime = calendar.time,
                            type = enumEventType.SCHEDULED_TASK,
                            id = task.id
                        )
                    }
                    // Update the task start date
                    task.startDate = Date(selectedDateMillis)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Row(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row (modifier = Modifier.weight(1f)){
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it }
            )
        }
        Column (modifier = Modifier.weight(4f)) {
            Text(
                text = task.taskName,
                fontSize = 16.sp,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
            // Max 2 lines for description
            Text(
                text = task.taskDescription,
                maxLines = 2,
                fontSize = 12.sp,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
        }
        Row (modifier = Modifier.weight(2f)) {
            IconButton(onClick = {
                // Show a dialog to select date
                showDatePicker = true
            }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Schedule")
            }
            IconButton(onClick = {
                /* Handle delete click */
                coroutineScope.launch {
                    scheduleDbViewModel.deleteById(task.id)
                    onDelete(task)
                }
            }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun SearchBarForTasks(modifier: Modifier) {
    var searchState by remember { mutableStateOf(TextFieldValue("")) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.inverseSurface,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                modifier = Modifier
                    .padding(8.dp)
                    .size(30.dp)
            )
            BasicTextField(
                value = searchState,
                onValueChange = { searchState = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                singleLine = true
            )
            Icon(
                painter = painterResource(id = R.drawable.baseline_mic_24),
                contentDescription = "Voice Icon",
                modifier = Modifier
                    .padding(8.dp)
                    .size(30.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Arrow Icon",
                modifier = Modifier
                    .padding(8.dp)
                    .size(30.dp)
            )
        }
    }
}

data class TaskDataForUi(
    val id: Long,
    val taskName: String,
    val taskDescription: String,
    val isChecked: Boolean,
    var startDate: Date?
)

