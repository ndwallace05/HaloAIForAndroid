package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme

val conversation_notes: StateFlow<List<ChatHistory.Message>>
    get() = _conversation

private val _conversation = MutableStateFlow(
    listOf(
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp,
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp,
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp,
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp,
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp,
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp,
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp,
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp,
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp,
        ChatHistory.Message.initConv,
        ChatHistory.Message.initConvResp
    ) // TODO: Replace with final initial message
)

@Composable
fun PlanWithHalo_Notes_Screen(navController: NavController, noteId: String) {

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
                ChatThreadForPlanWithHalo(
                    modifier = Modifier.fillMaxSize(),
                    model = ChatHistory(
                        messages = conversation_notes.collectAsState().value
                    )
                )
            }
        }
    }
}


@Composable
fun ChatThreadForPlanWithHalo(modifier: Modifier, model: ChatHistory){
    Box(modifier = modifier.fillMaxSize()) {

        LazyColumn {
            items(model.messages) { item ->
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    if (item.isUserMessage)
                        MessageBox(item, modifier = Modifier.align(Alignment.CenterEnd))
                    else
                        MessageBox(item, modifier = Modifier.align(Alignment.CenterStart))
                    // MessageBox(item)
                }
            }
        }

        InputBarForPlanWithHalo_Notes(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun InputBarForPlanWithHalo_Notes(modifier: Modifier) {
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
                modifier = Modifier.padding(8.dp).size(30.dp)
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
                modifier = Modifier.padding(8.dp).size(30.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Arrow Icon",
                modifier = Modifier.padding(8.dp).size(30.dp)
            )
        }
    }
}

