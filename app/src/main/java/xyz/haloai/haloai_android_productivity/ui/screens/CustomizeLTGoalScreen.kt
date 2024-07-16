package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import android.icu.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.data.local.entities.LTGoal
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme
import xyz.haloai.haloai_android_productivity.ui.viewmodel.LTGoalsViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel
import java.text.SimpleDateFormat
import java.util.Locale

val conversation_ltGoal: StateFlow<List<ChatHistoryForCustomizeLTGoal.Message>>
    get() = _conversation

private val _conversation = MutableStateFlow(
    listOf(
        ChatHistoryForCustomizeLTGoal.Message(
            displayText = "Hey there! I'm here to help you achieve this goal. What's on your mind?",
            completeText = null,
            isUserMessage = false
        ),
        )
    ) // TODO: Replace with final initial message

private val initialSeedPromptForAssistant = "You are a helpful assistant, whose task it is to help the user achieve a specific long term goal, by configuring weekly sessions to help them stay on track.\n" +
        "Your job is to have a conversation with the user, and update any of the following parameters which we use to plan their weekly sessions:\n" +
        "1. Context: This is any information about the user that you would need in a future conversation with the user to help them plan for this goal better.\n" +
        "2. Events per Week: Number of times they should work on this goal during the week.\n" +
        "3. Minutes per Week: Number of minutes they should spend in total to achieve this goal (this should always be a multiple of 15).\n" +
        "4. Min minutes per event: Minimum length of time for a block of time on their calendar for this goal.\n" +
        "5. Max minutes per event: Maximum length of time for a block of time on their calendar for this goal.\n" +
        "6. Deadline: The deadline for this goal in YYYY/MM/DD format.\n" +
        "For each of the above, you can use the following functions to update them:\n" +
        "1. Context: update_context(text: String)\n" +
        "2. Events per Week: update_events_per_week(number: Int)\n" +
        "3. Minutes per Week: update_minutes_per_week(number: Int)\n" +
        "4. Min minutes per event: update_min_minutes_per_event(number: Int)\n" +
        "5. Max minutes per event: update_max_minutes_per_event(number: Int)\n" +
        "6. Deadline: update_deadline(dateStringInFormat: String)\n\n" +
        "INSTRUCTIONS: \n" +
        "Your primary role is to modify these parameters based on what they tell you, but you can engage in a conversation as well. \n" +
        "You can update any of the above mid-conversation, by responding in the following format: you can output any of the above (one-per-line) at the beginning of your response, after which you can generate the rest of what you want to respond to them with.\n" +
        "Remember to avoid storing the same information about them repeatedly (in context). And tell them if you make any changes conversationally as well, by saying things like \"Noted!\" (if you change context), or \"I'm changing your weekly time commitment to 45 minutes\".\n" +
        "It is YOUR JOB to plan a schedule, which we will do using the parameters mentioned above. SO do not ask the user to work harder, or give them tips. Anything scheduling related is your job.\n" +
        "Keep these conversations task focused. You are mainly there to help them alter their schedule by tuning these parameters.\n" +
        "Don't directly ask the user for the parameters, instead come up with your best guess, and ask for confirmation instead.\n" +
        "Here is a sample conversation:\n" +
        "User: \n" +
        "I'm feeling a bit overwhelmed with my goal.\n" +
        "Assistant: \n" +
        "update_minutes_per_week(45)\n" +
        "update_events_per_week(2)\n" +
        "update_min_minutes_per_event(15)\n" +
        "update_max_minutes_per_event(30)\n" +
        "I'm sorry to hear that! I've reduced your weekly time commitment to 45 minutes, with only 2 events per week. How does that sound?\n" +
        "User: That sounds great! Thanks!\n"

data class ChatHistoryForCustomizeLTGoal(
    val messages: List<Message>) {
    data class Message(
        val displayText: String,
        val completeText: String? = null,
        val isUserMessage: Boolean,
    ) {
        val isFromMe: Boolean
            get() = isUserMessage

        companion object {
            val initConv = Message(
                displayText = "Hey there. Tell me what you'd like me to take care of, I'm here to help!",
                isUserMessage = false
            )
            val initConvResp = Message(
                displayText = "I'm doing great, how about you?",
                isUserMessage = true
            )
        }
    }
}

@Composable
fun CustomizeLTGoalScreen(navController: NavController, ltGoalId: String) {

    val ltGoalsViewModel: LTGoalsViewModel = koinInject()
    val openAIViewModel: OpenAIViewModel = koinInject()
    var currentGoalData by remember { mutableStateOf<LTGoal?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var firstMessageForGPT by remember { mutableStateOf("") }

    fun resetConversation() {
        _conversation.value = listOf(
            ChatHistoryForCustomizeLTGoal.Message(
                displayText = "Hey there! I'm here to help you achieve this goal. What's on your mind?",
                completeText = null,
                isUserMessage = false
            )
        )
    }

    // Use DisposableEffect to save the note when the composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            resetConversation()
        }
    }

    // Use OnLifecycleEvent to save the note when the lifecycle event occurs
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_ANY) {
                resetConversation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Add a listener to save the note when the user navigates away
    DisposableEffect(navController) {
        val callback = NavController.OnDestinationChangedListener { _, _, _ ->
            resetConversation()
        }
        navController.addOnDestinationChangedListener(callback)

        onDispose {
            navController.removeOnDestinationChangedListener(callback)
        }
    }

    fun getResponseAndUpdateConversation(message: String) {
        _conversation.value += ChatHistoryForCustomizeLTGoal.Message(message, null,true)
        isLoading = true
        coroutineScope.launch {
            var conversationText = firstMessageForGPT + "\n"
            for (message in _conversation.value.subList(1, _conversation.value.size)) {
                conversationText += if (message.isUserMessage) "User: \n${message.displayText}\n" else "Assistant: \n${message.completeText}\n"
            }
            val response = openAIViewModel.getChatGPTResponse(initialSeedPromptForAssistant, conversationText, modelToUse = "gpt-4o", temperature = 0.5)
            // Get Actions if any, and update the conversation
            val potentialActions = response.split("\n")
            var responseToUser = ""
            for (potentialAction in potentialActions) {
                if (potentialAction.startsWith("update_")) {
                    when {
                        potentialAction.startsWith("update_context") -> {
                            val newContext = currentGoalData!!.context + " " + potentialAction.split("(")[1].trim().removeSuffix(")").removePrefix("\"").removeSuffix("\"")
                            ltGoalsViewModel.updateLTGoal(
                                id = currentGoalData!!.id,
                                content = newContext
                            )
                        }
                        potentialAction.startsWith("update_events_per_week") -> {
                            val newEventsPerWeek = potentialAction.split("(")[1].trim().removeSuffix(")").toInt()
                            ltGoalsViewModel.updateLTGoal(
                                id = currentGoalData!!.id,
                                eventsPerWeek = newEventsPerWeek
                            )
                        }
                        potentialAction.startsWith("update_minutes_per_week") -> {
                            val newMinutesPerWeek = potentialAction.split("(")[1].trim().removeSuffix(")").toInt()
                            ltGoalsViewModel.updateLTGoal(
                                id = currentGoalData!!.id,
                                minutesPerWeek = newMinutesPerWeek
                            )
                        }
                        potentialAction.startsWith("update_min_minutes_per_event") -> {
                            val newMinMinutesPerEvent = potentialAction.split("(")[1].trim().removeSuffix(")").toInt()
                            ltGoalsViewModel.updateLTGoal(
                                id = currentGoalData!!.id,
                                minMinutesPerEvent = newMinMinutesPerEvent
                            )
                        }
                        potentialAction.startsWith("update_max_minutes_per_event") -> {
                            val newMaxMinutesPerEvent = potentialAction.split("(")[1].trim().removeSuffix(")").toInt()
                            ltGoalsViewModel.updateLTGoal(
                                id = currentGoalData!!.id,
                                maxMinutesPerEvent = newMaxMinutesPerEvent
                            )
                        }
                        potentialAction.startsWith("update_deadline") -> {
                            val newDeadline = potentialAction.split("(")[1].trim().removeSuffix(")").removePrefix("\"").removeSuffix("\"")
                            ltGoalsViewModel.updateLTGoal(
                                id = currentGoalData!!.id,
                                deadline = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(newDeadline)
                            )
                        }
                        // Default case
                        else -> {
                            responseToUser += potentialAction + "\n"
                        }
                    }
                }
                else {
                    responseToUser += potentialAction + "\n"
                }
            }
            _conversation.value += ChatHistoryForCustomizeLTGoal.Message(
                displayText = responseToUser,
                completeText = response,
                isUserMessage = false
            )
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        var dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        coroutineScope.launch {
            currentGoalData = ltGoalsViewModel.getLTGoalById(ltGoalId.toLong())
            val oneYearFromNow = Calendar.getInstance()
            oneYearFromNow.add(Calendar.YEAR, 1)
            val deadlineToUse = currentGoalData!!.deadline ?: oneYearFromNow
            firstMessageForGPT = "Below is a some information about the specific long-term goal they need help with:\n" +
                    "Title: ${currentGoalData?.title}\n" +
                    "Context: ${currentGoalData?.context}\n" +
                    "Events per Week: ${currentGoalData?.eventsPerWeek}\n" +
                    "Minutes per Week: ${currentGoalData?.minutesPerWeek}\n" +
                    "Min minutes per event: ${currentGoalData?.minMinutesPerEvent}\n" +
                    "Max minutes per event: ${currentGoalData?.maxMinutesPerEvent}\n" +
                    "Date Created: ${dateFormatter.format(currentGoalData?.dateCreated!!)}\n" +
                    "Deadline: ${dateFormatter.format(deadlineToUse)}\n" +
                    "Here is some information for you to get started with:\n" +
                    "Today's Date: ${dateFormatter.format(Calendar.getInstance().time)}\n"
            // Get a response from the assistant
            isLoading = false
        }
    }

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
                ChatThreadForCustomizePlan(
                    modifier = Modifier.fillMaxSize(),
                    model = ChatHistoryForCustomizeLTGoal(
                        messages = conversation_ltGoal.collectAsState().value
                    ),
                    onTextInput = { getResponseAndUpdateConversation(it) }
                )
            }
            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
fun ChatThreadForCustomizePlan(modifier: Modifier, model: ChatHistoryForCustomizeLTGoal, onTextInput: (String) -> Unit = { }){
    Box(modifier = modifier.fillMaxSize()) {

        LazyColumn {
            items(model.messages) { item ->
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                    if (item.isUserMessage)
                        MessageBox(item.displayText, item.isUserMessage, modifier = Modifier.align(Alignment.CenterEnd))
                    else
                        MessageBox(item.displayText, item.isUserMessage, modifier = Modifier.align(Alignment.CenterStart))
                    // MessageBox(item)
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        InputBarForCustomizePlan_LTGoal(modifier = Modifier.align(Alignment.BottomCenter), onTextInput = onTextInput)
    }
}

@Composable
fun InputBarForCustomizePlan_LTGoal(modifier: Modifier, onTextInput: (String) -> Unit = { }) {
    var searchState by remember { mutableStateOf(TextFieldValue("")) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
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
                painter = painterResource(id = R.drawable.haloai_logo),
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
            IconButton(
                onClick =
                {
                    onTextInput(searchState.text)
                    searchState = TextFieldValue("")
                },
                modifier = Modifier
                    .padding(8.dp)
                    .size(30.dp)
            ){
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Arrow Icon",
                )
            }
        }
    }
}


