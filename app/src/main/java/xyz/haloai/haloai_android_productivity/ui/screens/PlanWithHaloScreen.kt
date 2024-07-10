package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme
import xyz.haloai.haloai_android_productivity.ui.viewmodel.LTGoalsViewModel
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.PlanWithHaloScreenNav
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PlanWithHaloScreen(navController: NavController) {
    val ltGoalsViewModel: LTGoalsViewModel = koinInject()
    val coroutineScope = rememberCoroutineScope()

    val allLTGoals = remember { mutableStateListOf<LTGoalForUI>() }
    var refreshData by remember { mutableStateOf(true) }

    LaunchedEffect(refreshData) {
        if (refreshData) {
            allLTGoals.clear()
            coroutineScope.launch {
                allLTGoals.addAll(ltGoalsViewModel.getAllLTGoals().map {
                    LTGoalForUI(
                        id = it.id,
                        title = it.title,
                        dateCreated = it.dateCreated,
                        deadline = it.deadline,
                        eventsPerWeek = it.eventsPerWeek,
                        minutesPerWeek = it.minutesPerWeek
                    )
                })
            }
            refreshData = false
        }
    }

    HaloAI_Android_ProductivityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // val ltGoalsList = listOf("GRE", "Gym Coach", "SF Trip")
                /*val ltGoalsDescriptionList = listOf(
                    "Prepare for GRE by 31 Dec 2024",
                    "Fitness",
                    "Trip on 15 Jan 2025"
                )*/

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    /*item {
                        Text(
                            text = "Let's make a plan",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }*/
                    items (allLTGoals.size) { index ->
                        LTGoalCard(
                            ltGoal = allLTGoals[index],
                            onCustomizeClick = { navController.navigate(PlanWithHaloScreenNav.CustomizeLTGoal.createRoute(allLTGoals[index].id.toString())) },
                            onViewScheduleClick = {},
                            onMarkAsDoneClick = {}
                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        // Add a dummy note to the list
                        coroutineScope.launch {
                            // TODO: Replace with flow to allow text entry for a new note
                            val date = Calendar.getInstance().apply {
                                set(Calendar.YEAR, 2024)
                                set(Calendar.MONTH, 12)
                                set(Calendar.DAY_OF_MONTH, 31)
                            }.time
                            ltGoalsViewModel.insert("GRE", "Prepare for GRE by 31 Dec 2024", date)
                            refreshData = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 72.dp, end = 16.dp),
                    content = {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Icon"
                        )
                    }
                )

                ChatWithHaloBar(modifier = Modifier.align(Alignment.BottomCenter))

            }
        }
    }
}

@Composable
fun LTGoalCard(
    ltGoal: LTGoalForUI,
    onCustomizeClick: () -> Unit,
    onViewScheduleClick: () -> Unit,
    onMarkAsDoneClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = ltGoal.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            val dateFormatter = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            // 1 year from now
            var oneYearFromNow = Calendar.getInstance().apply {
                add(Calendar.YEAR, 1)
            }.time
            var deadlineToUse = ltGoal.deadline ?: oneYearFromNow
            val desc = "Created on ${dateFormatter.format(ltGoal.dateCreated)} | Deadline: " +
                    "${dateFormatter.format(deadlineToUse)} | ${ltGoal.eventsPerWeek} events per week | ${ltGoal.minutesPerWeek} " +
                    "minutes per week"
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconTextButton(
                    icon = Icons.Default.Edit,
                    text = "Customize",
                    onClick = onCustomizeClick
                )
                IconTextButton(
                    icon = Icons.Default.Menu,
                    text = "View Schedule",
                    onClick = onViewScheduleClick
                )
                IconTextButton(
                    icon = Icons.Default.CheckCircle,
                    text = "Mark as Done",
                    onClick = onMarkAsDoneClick
                )
            }
        }
    }
}

@Composable
fun IconTextButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ChatWithHaloBar(modifier: Modifier) {
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
                painter = painterResource(id = R.drawable.long_term_goals),
                contentDescription = "LT Goals Icon",
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

data class LTGoalForUI(
    val id: Long,
    val title: String,
    val dateCreated: Date,
    val deadline: Date?,
    val eventsPerWeek: Int,
    val minutesPerWeek: Int,
)