package xyz.haloai.haloai_android_productivity.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.common.collect.ImmutableList
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.ui.theme.HaloAI_Android_ProductivityTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.exp

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
                    // This param contains
                    // all the dates
                    // that we want to show. We can use this to expand the view to show the
                    // entire month.
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
                // A bar (small rectangle, rounded corners) which when clicked will expand the
                // calendar
                // to show the entire month
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
                EventsView(selectedDate = selectedDate.value)
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
    TODO("Not yet implemented")
}

private fun Date.formatToCalendarDay(): String = SimpleDateFormat("d", Locale.getDefault()).format(this)

fun generateImmutableListOfDates(selectedDate: Date, calendarExpanded: Boolean):
        ImmutableList<Pair<Date, Boolean>> {
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate
    if (!calendarExpanded)
    {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        calendar.add(Calendar.DATE, -dayOfWeek + 1)
        val datesForThisWeekStartingSunday = mutableListOf<Pair<Date, Boolean>>()
        for (i in 1..7) {
            if (calendar.time == selectedDate) {
                datesForThisWeekStartingSunday.add(calendar.time to true)
            } else {
                datesForThisWeekStartingSunday.add(calendar.time to false)
            }
            // datesForThisWeekStartingSunday.add(calendar.time to false)
            calendar.add(Calendar.DATE, 1)
        }
        return ImmutableList.copyOf(datesForThisWeekStartingSunday)
    }
    else {
        val firstDayOfMonth = Calendar.getInstance().apply {
            time = selectedDate
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val datesForThisMonth = mutableListOf<Pair<Date, Boolean>>()
        do {
            if (firstDayOfMonth.time == selectedDate) {
                datesForThisMonth.add(firstDayOfMonth.time to true)
            } else {
                datesForThisMonth.add(firstDayOfMonth.time to false)
            }
            // datesForThisMonth.add(firstDayOfMonth.time to false)
            firstDayOfMonth.add(Calendar.DATE, 1)
        }
        while(firstDayOfMonth.get(Calendar.DAY_OF_MONTH) != 1)
        return ImmutableList.copyOf(datesForThisMonth)
    }
}

@Composable
fun Float.toPx(): Float {
    val density = LocalDensity.current
    return with(density) { this@toPx.toDp().toPx() }
}


@Composable
private fun CalendarCell(
    date: Date,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = date.formatToCalendarDay()
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize()
            .padding(2.dp)
            .background(
                shape = RoundedCornerShape(CornerSize(8.dp)),
                color = colorScheme.secondaryContainer,
            )
            .clip(RoundedCornerShape(CornerSize(8.dp)))
            .clickable(onClick = onClick)
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(
                        shape = CircleShape,
                        color = colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                    )
            )
        }
        Text(
            text = text,
            color = colorScheme.onSecondaryContainer,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private fun Int.getDayOfWeek3Letters(): String? = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_WEEK, this@getDayOfWeek3Letters)
}.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())

@Composable
private fun WeekdayCell(weekday: Int, modifier: Modifier = Modifier) {
    val text = weekday.getDayOfWeek3Letters()
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize()
    ) {
        Text(
            text = text.orEmpty(),
            color = colorScheme.onPrimaryContainer,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


fun formatToWeekDay(date: Date): String {
    val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    return dateFormat.format(date)
}

@Composable
private fun CalendarGrid(
    date: ImmutableList<Pair<Date, Boolean>>,
    onClick: (Date) -> Unit,
    startFromSunday: Boolean,
    modifier: Modifier = Modifier,
) {
    val weekdayFirstDay = formatToWeekDay(date.first().first).let {
        when (it) {
            "Sunday" -> 1
            "Monday" -> 2
            "Tuesday" -> 3
            "Wednesday" -> 4
            "Thursday" -> 5
            "Friday" -> 6
            "Saturday" -> 7
            else -> 1
        }
    }
    val weekdays = getWeekDays(startFromSunday)
    CalendarCustomLayout(modifier = modifier) {
        weekdays.forEach {
            WeekdayCell(weekday = it)
        }
        // Adds Spacers to align the first day of the month to the correct weekday
        repeat(if (!startFromSunday) weekdayFirstDay - 2 else weekdayFirstDay - 1) {
            Spacer(modifier = Modifier)
        }
        date.forEach {
            CalendarCell(date = it.first, selected = it.second, onClick = { onClick(it.first) })
        }
    }
}

fun getWeekDays(startFromSunday: Boolean): ImmutableList<Int> {
    val lista = (1..7).toList()
    return if (startFromSunday) {
        ImmutableList.copyOf(lista)
    } else {
        ImmutableList.copyOf(lista.drop(1) + lista.take(1))
    }
}

@Composable
private fun CalendarCustomLayout(
    modifier: Modifier = Modifier,
    horizontalGapDp: Dp = 2.dp,
    verticalGapDp: Dp = 2.dp,
    content: @Composable () -> Unit,
) {
    val horizontalGap = with(LocalDensity.current) {
        horizontalGapDp.roundToPx()
    }
    val verticalGap = with(LocalDensity.current) {
        verticalGapDp.roundToPx()
    }
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        val totalWidthWithoutGap = constraints.maxWidth - (horizontalGap * 6)
        val singleWidth = totalWidthWithoutGap / 7

        val xPos: MutableList<Int> = mutableListOf()
        val yPos: MutableList<Int> = mutableListOf()
        var currentX = 0
        var currentY = 0
        measurables.forEach { _ ->
            xPos.add(currentX)
            yPos.add(currentY)
            if (currentX + singleWidth + horizontalGap > totalWidthWithoutGap) {
                currentX = 0
                currentY += singleWidth + verticalGap
            } else {
                currentX += singleWidth + horizontalGap
            }
        }

        val placeables: List<Placeable> = measurables.map { measurable ->
            measurable.measure(constraints.copy(maxHeight = singleWidth, maxWidth = singleWidth))
        }

        layout(
            width = constraints.maxWidth,
            height = currentY + singleWidth + verticalGap,
        ) {
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative(
                    x = xPos[index],
                    y = yPos[index],
                )
            }
        }
    }
}

@Composable
fun CalendarView(
    month: Date,
    date: ImmutableList<Pair<Date, Boolean>>?,
    displayNext: Boolean,
    displayPrev: Boolean,
    onClickNext: () -> Unit,
    onClickPrev: () -> Unit,
    onClick: (Date) -> Unit,
    startFromSunday: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (displayPrev)
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { onClickPrev() },
                    contentDescription = "navigate to previous month"
                )
            if (displayNext)
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onClickNext() },
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "navigate to next month"
                )
            Text(
                text = month.formatToMonthString(),
                style = typography.headlineMedium,
                color = colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        if (!date.isNullOrEmpty()) {
            CalendarGrid(
                date = date,
                onClick = onClick,
                startFromSunday = startFromSunday,
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

fun Date.formatToMonthString(): String = SimpleDateFormat("MMMM", Locale.getDefault()).format(this)