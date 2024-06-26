package xyz.haloai.haloai_android_productivity.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun NotesScreen_GenerateTasksPopup(noteId: String, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        var newTaskText by remember { mutableStateOf(TextFieldValue("")) }
        var tasks by remember {
            mutableStateOf(
                listOf(
                    "Decide places to visit",
                    "Book Flights",
                    "Create Budget",
                    "Finalize Dates"
                )
            )
        }
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
                item {
                    Text(
                        text = "Suggested Tasks",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .alpha(0.8f)
                    )
                }

                items(tasks) { task ->
                    TaskItem(task)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    TextField(
                        value = newTaskText,
                        onValueChange = {
                            newTaskText = it
                        },
                        placeholder = { Text("Enter Task Here...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newTaskText.text.isNotBlank()) {
                                    tasks = tasks + newTaskText.text
                                    newTaskText = TextFieldValue("") // Reset the text field
                                }
                            }
                        )
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Button(
                        onClick = { /* Add task logic */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Tasks")
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = true,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            ),
            onClick = { /* Handle selection change */ }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(task, modifier = Modifier.padding(1.dp), color = MaterialTheme.colorScheme
            .onPrimaryContainer, fontSize = 12.sp)
    }
}

