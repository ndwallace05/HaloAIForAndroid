package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.NotesDetailsScreenNav

@Composable
fun NoteDetailsScreen(navController: NavController, noteId: String) {
    // TODO: Fetch note details from database
    var title by remember { mutableStateOf(TextFieldValue("Europe Trip")) }
    var content by remember { mutableStateOf(TextFieldValue("List of things to do in Europe\n\n• Colosseum\n• Decide Cities\n• Visa\n• Dates\n• Flights\n  • Go on 15th Jan?\n• Budget\n• Places to visit:\n  • A\n  • B\n  • C\n  • D")) }
    var showGenerateTasksPopup by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth
                    (), horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = { showGenerateTasksPopup = true }, modifier =
                Modifier
                    .background(MaterialTheme
                    .colorScheme.primary, shape = RoundedCornerShape(8.dp))) {
                    Text(text = "Generate Tasks", modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { navController.navigate(
                    NotesDetailsScreenNav.PlanWithHalo
                    .createRoute(noteId)) },
                    modifier =
                Modifier.background
                    (MaterialTheme
                    .colorScheme
                    .primary, shape = RoundedCornerShape(8.dp))) {
                    Text(text = "Plan with Halo", modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Star", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Summary Of Note...", color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.fillMaxWidth().alpha(0.5f))
                }
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp)
                )
            }
            if (showGenerateTasksPopup) {
                NotesScreen_GenerateTasksPopup(noteId, onDismissRequest = { showGenerateTasksPopup =
                    false })
            }
        }
    }
}