package xyz.haloai.haloai_android_productivity.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.koin.compose.koinInject
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType
import xyz.haloai.haloai_android_productivity.data.ui.viewmodel.EmailDbViewModel

@Composable
fun SelectCalendarIdsDialog(emailId: String, calendarIds: MutableList<Pair<String, String>>, onDismissRequest: () -> Unit) {
    val isCalendarIdSelected = remember { mutableStateOf<List<Boolean>>(List(calendarIds.size) { false }) }
    val emailDbViewModel: EmailDbViewModel = koinInject()

    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(2.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(calendarIds.size) { idx ->
                    val modifier = Modifier.clickable {
                        isCalendarIdSelected.value =
                            isCalendarIdSelected.value.toMutableList().apply {
                                this[idx] = !this[idx]
                            }
                    }
                    CalendarIdCard(
                        calIdName = calendarIds[idx].second,
                        isSelected = isCalendarIdSelected
                            .value[idx],
                        modifier = modifier,
                        onClick = {
                            isCalendarIdSelected.value =
                                isCalendarIdSelected.value.toMutableList().apply {
                                    this[idx] = !this[idx]
                                }
                        })
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Button(
                        onClick =
                        {
                            val finalCalendarIdsSelected = mutableListOf<String>()
                            for (i in 0 until calendarIds.size) {
                                if (isCalendarIdSelected.value[i]) {
                                    finalCalendarIdsSelected.add(calendarIds[i].first)
                                }
                            }
                            // Insert the selected email ID and calendar IDs into the database
                            emailDbViewModel.insert(emailId, finalCalendarIdsSelected,
                                enumEmailType.MICROSOFT)
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(8.dp).background(
                            MaterialTheme
                            .colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    )
                    {
                        Text("Add", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}