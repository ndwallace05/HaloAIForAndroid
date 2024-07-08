package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.data.local.entities.enumFeedCardType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumImportanceScore
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ProductivityFeedOptionsViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ProductivityFeedViewModel
import java.util.Date
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HomeScreen(navController: NavController) {
    val productivityFeedViewModel: ProductivityFeedViewModel = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val cardsToDisplay = remember { mutableStateListOf<FeedCardDataForUi>() }
    val extraCards = remember { mutableStateListOf<FeedCardDataForUi>() }
    val showSeeMore = remember { mutableStateOf(false) }
    val numCardsToDisplayInitially = 10
    val showOptionsList = remember { mutableStateOf(false) }
    val selectedCardId = remember { mutableLongStateOf(-1L) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val allCards = productivityFeedViewModel.getAllFeedCards()
            cardsToDisplay.clear()
            extraCards.clear()
            for (card in allCards.subList(0, numCardsToDisplayInitially.coerceAtMost(allCards.size))) {
                cardsToDisplay.add(
                    FeedCardDataForUi(
                        id = card.id,
                        title = card.title,
                        description = card.description,
                        extraDescription = card.extraDescription,
                        importanceScore = card.importanceScore,
                        primaryActionType = card.primaryActionType,
                        imgB64 = card.imgBase64,
                        deadline = card.deadline)
                )
            }
            if (allCards.size > numCardsToDisplayInitially) {
                showSeeMore.value = true
                for (card in allCards.subList(numCardsToDisplayInitially, allCards.size)) {
                    extraCards.add(
                        FeedCardDataForUi(
                            id = card.id,
                            title = card.title,
                            description = card.description,
                            extraDescription = card.extraDescription,
                            importanceScore = card.importanceScore,
                            primaryActionType = card.primaryActionType,
                            imgB64 = card.imgBase64,
                            deadline = card.deadline)
                    )
                }
            }
        }
    }

    fun onDismissCard(card: FeedCardDataForUi, removeFromDb: Boolean = true) {
        // Handle dismiss action here
        cardsToDisplay.remove(card)
        if (removeFromDb)
        {
            coroutineScope.launch {
                productivityFeedViewModel.deleteFeedCardById(card.id)
            }
        }
        selectedCardId.value = -1
        showOptionsList.value = false
    }

    fun showOptions(id: Long) {
        showOptionsList.value = true
        selectedCardId.longValue = id
    }

    HaloAI_Android_ProductivityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn {
                    items(cardsToDisplay.size) {
                        val card = cardsToDisplay[it]
                        ProductivityFeedCard(card = card, onDismiss = ::onDismissCard,
                            showOptionsFun = ::showOptions, context = context)
                    }
                    if (showSeeMore.value) {
                        item {
                            Column()
                            {
                                Button(
                                    onClick = {
                                        cardsToDisplay.addAll(extraCards)
                                        extraCards.clear()
                                        showSeeMore.value = false
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(text = "See More")
                                }
                            }
                        }
                    }
                }

                if (showOptionsList.value) {
                    // Show options list
                    Dialog(onDismissRequest = { showOptionsList.value = false }) {
                        // Draw a rectangle shape with rounded corners inside the dialog
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(375.dp)
                                .padding(2.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            val selectedCard = cardsToDisplay.find { it.id == selectedCardId.value }!!
                            val optionsList = getOptionDetails(
                                context = context,
                                card = selectedCard,
                                showOptionsFun = { },
                                getAllOptions = true,
                                onDismissFun = ::onDismissCard
                            ).allOptions
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                items (optionsList.size) {
                                    val option = optionsList[it]
                                    val modifier = Modifier.clickable {
                                        option.optionClickFunction(selectedCard)
                                        showOptionsList.value = false
                                    }
                                    Card(
                                        modifier = modifier,
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = CardDefaults.cardElevation(4.dp),
                                        colors = CardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = option.optionText,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        // Add a dummy note to the list
                        coroutineScope.launch {
                            // TODO: Replace with flow to allow text entry for a new note
                            productivityFeedViewModel.insertFeedCard(
                                title = "File tax returns",
                                description = "File tax returns for 2023",
                                extraDescription = "",
                                primaryActionType = enumFeedCardType.POTENTIAL_TASK,
                                importanceScore = 4,
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 16.dp),
                    content = {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Icon"
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun ProductivityFeedCard(card: FeedCardDataForUi, onDismiss: (FeedCardDataForUi, Boolean) -> Unit,
                         showOptionsFun: (Long) -> Unit, context: Context) {
    // val context = LocalContext.current
    val optionDetails = getOptionDetails(
        context = context,
        card = card,
        showOptionsFun = showOptionsFun,
        getAllOptions = false,
        onDismissFun = onDismiss
    )
    val swipeableState = rememberSwipeableState(0)
    val sizePx = with(LocalDensity.current) { 300.dp.toPx() }
    val anchors = mapOf(0f to 0, sizePx to 1, -sizePx to -1)
    val scope = rememberCoroutineScope()

    val offsetX by animateFloatAsState(targetValue = swipeableState.offset.value, label = "")
    val rotationAngle by animateFloatAsState(targetValue = (swipeableState.offset.value / sizePx) * 15,
        label = ""
    )

    val snapBackThreshold = sizePx * 0.3f

    LaunchedEffect(swipeableState.offset.value) {
        if (swipeableState.offset.value != 0f && abs(swipeableState.offset.value) < snapBackThreshold) {
            swipeableState.snapTo(0)
        }
    }

    Box (
        modifier = Modifier.padding(4.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .rotate(rotationAngle),
    ) {
        if (swipeableState.currentValue == 1) {
            // Swiped right (accept)
            LaunchedEffect(Unit) {
                // Handle accept action
                optionDetails.optionClickFunction(card)
            }
        } else if (swipeableState.currentValue == -1) {
            // Swiped left (dismiss)
            LaunchedEffect(Unit) {
                // Handle dismiss action
                onDismiss(card, false)
            }
        }

        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Horizontal
                ),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (optionDetails.logoResource != null) {
                        Image(
                            painter = painterResource(id = optionDetails.logoResource),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else if (optionDetails.logoVector != null) {
                        Image(
                            imageVector = optionDetails.logoVector,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    val color = when (card.importanceScore) {
                        enumImportanceScore.VERY_LOW -> Color.Blue
                        enumImportanceScore.LOW -> Color.Green
                        enumImportanceScore.MEDIUM -> Color.Yellow
                        enumImportanceScore.HIGH -> Color(0xFFFFA500) // Orange
                        enumImportanceScore.URGENT -> Color.Red
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Base64Image(card.imgB64, Modifier.fillMaxSize())
                    /*Image(
                    painter = painterResource(id = R.drawable.haloai_logo),
                    contentDescription = card.title,
                    modifier = ,
                    contentScale = ContentScale.Crop
                )*/
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = card.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = card.description,
                    fontWeight = FontWeight.Thin,
                    fontSize = 14.sp,
                    // color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = card.extraDescription,
                    fontSize = 14.sp,
                    // Italic
                    fontStyle = FontStyle.Italic,
                    // color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { optionDetails.optionClickFunction(card) },
                        modifier = Modifier.weight(4f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = optionDetails.optionText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick =
                        {
                            showOptionsFun(card.id)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme
                            .colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Base64Image(base64String: String, modifier: Modifier = Modifier) {
    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

data class FeedCardDataForUi(
    val id: Long,
    val title: String,
    val description: String,
    val extraDescription: String,
    val deadline: Date? = null,
    val importanceScore: enumImportanceScore,
    val primaryActionType: enumFeedCardType,
    val imgB64: String
)

data class OptionDetails(
    val optionType: enumFeedCardType,
    val optionText: String,
    val optionClickFunction: (FeedCardDataForUi) -> Unit,
    val logoResource: Int?,
    val logoVector: ImageVector?,
    val allOptions: MutableList<OptionDetails> = mutableListOf()
)

@Composable
fun getOptionDetails(context: Context, card: FeedCardDataForUi, showOptionsFun: (Long) -> Unit,
                     getAllOptions: Boolean = false, onDismissFun: (FeedCardDataForUi, Boolean) ->
    Unit)
: OptionDetails {
    val productivityFeedOptionsViewModel: ProductivityFeedOptionsViewModel = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val optionsList = mutableListOf<OptionDetails>(
        OptionDetails(
            optionType = enumFeedCardType.POTENTIAL_NOTE,
            optionText = "Add to notes",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToNote(
                        title = it.title,
                        description = it.description,
                        extraDescription = it.extraDescription
                    )
                }
                onDismissFun(it, true)
            },
            logoResource = R.drawable.notes,
            logoVector = null
        ),
        OptionDetails(
            optionType = enumFeedCardType.POTENTIAL_EVENT,
            optionText = "Add to calendar",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToCalendarAsEvent(
                        title = it.title,
                        description = it.description,
                        extraDescription = it.extraDescription,
                        context = context
                    )
                }
                onDismissFun(it, true)

            },
            logoResource = null,
            logoVector = Icons.Default.DateRange
        ),
        OptionDetails(
            optionType = enumFeedCardType.POTENTIAL_LTGOAL,
            optionText = "Create a goal",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToLTGoals(
                        title = it.title,
                        description = it.description,
                        extraDescription = it.extraDescription,
                        deadline = it.deadline,
                        priority = it.importanceScore.ordinal,
                        context = context
                    )
                }
                onDismissFun(it, true)
            },
            logoResource = R.drawable.long_term_goals,
            logoVector = null
        ),
        OptionDetails(
            optionType = enumFeedCardType.POTENTIAL_TASK,
            optionText = "Add to tasks",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToTasks(
                        title = it.title,
                        description = it.description,
                        extraDescription = it.extraDescription,
                        deadline = it.deadline,
                        priority = it.importanceScore.ordinal,
                        context = context
                    )
                    onDismissFun(it, true)
                }
            },
            logoResource = R.drawable.tasks,
            logoVector = null
        ),
        OptionDetails(
            optionType = enumFeedCardType.OTHER,
            optionText = "Choose Option",
            optionClickFunction =
            {
                // Show popup to choose an option
                showOptionsFun(card.id)
            },
            logoResource = R.drawable.haloai_logo,
            logoVector = null
        ),
        OptionDetails(
            optionType = enumFeedCardType.QUOTE,
            optionText = "Save Quote",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToNote(
                        title = "Quote",
                        description = it.title,
                        extraDescription = it.description
                    )
                }
                onDismissFun(it, true)
            },
            logoResource = null,
            logoVector = Icons.Default.FavoriteBorder
        ),
        OptionDetails(
            optionType = enumFeedCardType.NEWSLETTER,
            optionText = "Read more",
            optionClickFunction =
            {
                Toast.makeText(context, "Read more", Toast.LENGTH_SHORT).show()
                onDismissFun(it, true)
            },
            logoResource = null,
            logoVector = Icons.Default.Email
        ),
        OptionDetails(
            optionType = enumFeedCardType.YOUTUBE_VIDEO,
            optionText = "Watch video",
            optionClickFunction =
            {
                Toast.makeText(context, "Watch video", Toast.LENGTH_SHORT).show()
                onDismissFun(it, true)
            },
            logoResource = null,
            logoVector = Icons.Default.PlayArrow
        )
    )

    if (getAllOptions) {
        return OptionDetails(
            optionType = enumFeedCardType.OTHER,
            optionText = "Choose Option",
            optionClickFunction =
            {
                // Show popup to choose an option
                showOptionsFun(card.id)
            },
            logoResource = R.drawable.haloai_logo,
            logoVector = null,
            allOptions = optionsList.filter { it.optionType != enumFeedCardType.OTHER }.toMutableList()
        )
    }
    else {
        return optionsList.find { it.optionType == card.primaryActionType }!!
    }

    /*return when (card.primaryActionType) {
        enumFeedCardType.POTENTIAL_NOTE -> OptionDetails(
            optionText = "Add to notes",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToNote(
                        title = it.title,
                        description = it.description,
                        extraDescription = it.extraDescription
                    )
                }
            },
            logoResource = R.drawable.notes,
            logoVector = null
        )
        enumFeedCardType.POTENTIAL_EVENT -> OptionDetails(
            optionText = "Add to calendar",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToCalendarAsEvent(
                        title = it.title,
                        description = it.description,
                        extraDescription = it.extraDescription,
                        context = context
                    )
                }
            },
            logoResource = null,
            logoVector = Icons.Default.DateRange
        )
        enumFeedCardType.POTENTIAL_LTGOAL -> OptionDetails(
            optionText = "Create a goal",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToLTGoals(
                        title = it.title,
                        description = it.description,
                        extraDescription = it.extraDescription,
                        deadline = it.deadline,
                        priority = it.importanceScore.ordinal,
                        context = context
                    )
                }
            },
            logoResource = R.drawable.long_term_goals,
            logoVector = null
        )
        enumFeedCardType.POTENTIAL_TASK, enumFeedCardType.TASK_SUGGESTION -> OptionDetails(
            optionText = "Add to tasks",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToTasks(
                        title = it.title,
                        description = it.description,
                        extraDescription = it.extraDescription,
                        deadline = it.deadline,
                        priority = it.importanceScore.ordinal,
                        context = context
                    )
                }
            },
            logoResource = R.drawable.tasks,
            logoVector = null
        )
        enumFeedCardType.OTHER -> OptionDetails(
            optionText = "Choose Option",
            optionClickFunction =
            {
                // Show popup to choose an option
                showOptionsFun(card.id)
            },
            logoResource = R.drawable.haloai_logo,
            logoVector = null
        )
        enumFeedCardType.QUOTE -> OptionDetails(
            optionText = "Save Quote",
            optionClickFunction =
            {
                coroutineScope.launch {
                    productivityFeedOptionsViewModel.addToNote(
                        title = "Quote",
                        description = it.title,
                        extraDescription = it.description
                    )
                }
            },
            logoResource = null,
            logoVector = Icons.Default.FavoriteBorder
        )
        enumFeedCardType.NEWSLETTER -> OptionDetails(
            optionText = "Read more",
            optionClickFunction = { Toast.makeText(context, "Read more", Toast.LENGTH_SHORT).show() },
            logoResource = null,
            logoVector = Icons.Default.Email
        )
        enumFeedCardType.YOUTUBE_VIDEO -> OptionDetails(
            optionText = "Watch video",
            optionClickFunction = { Toast.makeText(context, "Watch video", Toast.LENGTH_SHORT).show() },
            logoResource = null,
            logoVector = Icons.Default.PlayArrow
        )
    }*/
}