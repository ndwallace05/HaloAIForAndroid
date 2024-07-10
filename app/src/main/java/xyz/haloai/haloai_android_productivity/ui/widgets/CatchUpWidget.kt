package xyz.haloai.haloai_android_productivity.ui.widgets

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.MainActivity
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.data.local.entities.enumFeedCardType
import xyz.haloai.haloai_android_productivity.data.ui.compose_components.EventDataForUI
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ProductivityFeedViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.FeedCardDataForUi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CatchUpWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CatchUpWidget()
}

class CatchUpWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            CatchUpWidgetLayout(context)
        }
    }
}

@Composable
fun CatchUpWidgetLayout(context: Context) {
    val productivityFeedViewModel: ProductivityFeedViewModel = koinInject()
    val topNItems = remember { mutableStateListOf<FeedCardDataForUi>() }
    val eventsForTheDay = remember { mutableStateListOf<EventDataForUI>() }
    val cardsToShow = remember { mutableStateListOf<DataForCatchUpCard>() }
    val coroutineScope = rememberCoroutineScope()
    val scheduleDbViewModel: ScheduleDbViewModel = koinInject { parametersOf(context, false) }
    val numFeedCardsToShow = 3

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        Header()
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                val topNFeedCards = productivityFeedViewModel.getTopFeedCards(numFeedCardsToShow)
                topNItems.clear()
                for (feedCard in topNFeedCards) {
                    val topOptionLogo = when (feedCard.primaryActionType) {
                        enumFeedCardType.POTENTIAL_TASK -> R.drawable.tasks
                        enumFeedCardType.POTENTIAL_LTGOAL -> R.drawable.long_term_goals
                        enumFeedCardType.POTENTIAL_NOTE -> R.drawable.notes
                        else -> R.drawable.haloai_logo
                    }
                    cardsToShow.add(
                        DataForCatchUpCard(
                            title = feedCard.title,
                            drawable = topOptionLogo,
                            priority = feedCard.importanceScore.value,
                            type = EnumCatchUpCardType.FEED_CARD
                        )
                    )
                }
                /*topNItems.addAll(topNFeedCards.map { FeedCardDataForUi(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    extraDescription = it.extraDescription,
                    deadline = it.deadline,
                    importanceScore = it.importanceScore,
                    imgB64 = it.imgBase64,
                    primaryActionType = it.primaryActionType)
                })*/
            }
        }
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                val startDateTime = Calendar.getInstance().time
                val todayEodTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time
                val events = scheduleDbViewModel.getEventsBetween(startDateTime, todayEodTime)
                eventsForTheDay.clear()
                for (event in events) {
                    // Convert Date object to LocalDateTime
                    var start: LocalDateTime = event.startTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    var end: LocalDateTime = event.endTime!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    // If event starts in next 30 mins / is ongoing, give priority 5. Else, Give 4,3,2,1 based on how far it is from now to EOD.
                    val now = LocalDateTime.now()
                    val thirtyMinsLater = now.plusMinutes(30)
                    val eventPriority = when {
                        start.isBefore(now) && end.isAfter(now) -> 5
                        start.isBefore(thirtyMinsLater) -> 5
                        start.isBefore(now.plusHours(1)) -> 4
                        start.isBefore(now.plusHours(2)) -> 3
                        start.isBefore(now.plusHours(3)) -> 2
                        else -> 1
                    }
                    // Title: Start Time - End Time: Event Title
                    val titleToUse =
                    cardsToShow.add(
                        DataForCatchUpCard(
                            title = event.title,
                            drawable = R.drawable.haloai_logo,
                            priority = eventPriority,
                            type = EnumCatchUpCardType.EVENT,
                            startTime = event.startTime,
                            endTime = event.endTime
                        )
                    )
                }
            }
        }
        LazyColumn {
            cardsToShow.sortByDescending { it.priority }
            items(cardsToShow.size) { itemId ->
                CatchUpItem(cardsToShow[itemId])
            }
        }
    }
}

@Composable
fun Header() {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(color = MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(R.drawable.haloai_logo), // TODO: Replace with star icon
                // (R.drawable.ic_star)
                contentDescription = "Star Icon",
                modifier = GlanceModifier.size(24.dp)
            )
            Text(
                text = "Catch Up",
                modifier = GlanceModifier.padding(start = 8.dp),
                style =
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(MaterialTheme.colorScheme.onSurface)
            ))
        }
        /*Box(
            modifier = GlanceModifier
                .size(24.dp)
                .background(ColorProvider(Color(0xFF4CAF50)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "12", fontSize = 14.sp, color = ColorProvider(Color.White))
        }*/
    }
}

@Composable
fun CatchUpItem(cardData: DataForCatchUpCard) {
    val todayDateStart = Calendar.getInstance()
    todayDateStart.set(Calendar.HOUR_OF_DAY, 0)
    todayDateStart.set(Calendar.MINUTE, 0)
    todayDateStart.set(Calendar.SECOND, 0)
    todayDateStart.set(Calendar.MILLISECOND, 0)
    val todayDate = todayDateStart.time
    val eodTime = Calendar.getInstance()
    eodTime.set(Calendar.HOUR_OF_DAY, 23)
    eodTime.set(Calendar.MINUTE, 59)
    eodTime.set(Calendar.SECOND, 59)
    eodTime.set(Calendar.MILLISECOND, 999)

    var modifier = GlanceModifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(bottom = 8.dp, start = 4.dp, end = 4.dp)
        .padding(4.dp)
        .cornerRadius(12.dp)

    val assistantScreenParameterKey = ActionParameters.Key<String>("defaultDestination")
    val backgroundColor = when (cardData.type) {
        EnumCatchUpCardType.FEED_CARD -> MaterialTheme.colorScheme.secondaryContainer
        EnumCatchUpCardType.EVENT -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val fontColor = when (cardData.type) {
        EnumCatchUpCardType.FEED_CARD -> MaterialTheme.colorScheme.onSecondaryContainer
        EnumCatchUpCardType.EVENT -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    if (cardData.type == EnumCatchUpCardType.FEED_CARD) {
        modifier = modifier.clickable(onClick =
            actionStartActivity<MainActivity>(actionParametersOf(assistantScreenParameterKey to
                    "Screens.Home"))
        )
    } else if (cardData.type == EnumCatchUpCardType.EVENT) {
        modifier = modifier.clickable(onClick =
            actionStartActivity<MainActivity>(actionParametersOf(assistantScreenParameterKey to
                    "Screens.Calendar"))
        )
    }

    Box(
        modifier = modifier,
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(backgroundColor)
                .cornerRadius(8.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cardData.type == EnumCatchUpCardType.EVENT) {
                Column(
                    modifier = GlanceModifier.padding(2.dp).wrapContentHeight().width(60.dp)
                        .background(GlanceTheme.colors.inversePrimary).cornerRadius(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Format HH:mm in AM/PM from Date object
                    var startTimeInFormat = SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(cardData.startTime!!)
                    // If event started yesterday
                    if (cardData.startTime.before(todayDate)) {
                        startTimeInFormat = "(-1) $startTimeInFormat"
                    }
                    var endTimeInFormat = SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(cardData.endTime!!)
                    // If event ended tomorrow, show (+1) in front of time
                    if (cardData.endTime.after(eodTime.time)) {
                        endTimeInFormat = "(+1) $endTimeInFormat"
                    }
                    Text(
                        text = startTimeInFormat,
                        style = TextStyle(fontSize = 8.sp, color = GlanceTheme.colors.onSurface)
                    )
                    // Should atleast last for a minute
                    if ((cardData.endTime.time - cardData.startTime.time) >= 60000) {
                        Text(
                            text = endTimeInFormat,
                            style = TextStyle(fontSize = 8.sp, color = GlanceTheme.colors.onSurface)
                        )
                    }
                }
            }
            else {
                Image(
                    provider = ImageProvider(cardData.drawable),
                    contentDescription = "Outlook Icon",
                    modifier = GlanceModifier.size(24.dp).width(60.dp),
                    colorFilter = ColorFilter.tint(ColorProvider(fontColor))
                )
            }
            Text(
                text = cardData.title,
                style = TextStyle(
                    color = ColorProvider(fontColor),
                    fontSize = 16.sp
                ),
                modifier = GlanceModifier.padding(start = 8.dp)
            )
        }
    }
}

data class DataForCatchUpCard(
    val title: String,
    val drawable: Int,
    val priority: Int,
    val type: EnumCatchUpCardType,
    val startTime: Date? = null,
    val endTime: Date? = null
)

enum class EnumCatchUpCardType(val value: Int) {
    FEED_CARD(0),
    EVENT(1)
}