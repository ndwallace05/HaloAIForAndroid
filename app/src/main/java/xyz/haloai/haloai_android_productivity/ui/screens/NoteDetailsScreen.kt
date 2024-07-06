package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import xyz.haloai.haloai_android_productivity.ui.viewmodel.NotesDbViewModel
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.NotesDetailsScreenNav

@Composable
fun NoteDetailsScreen(navController: NavController, noteId: String) {
    val notesDbViewModel: NotesDbViewModel = koinInject()
    var note by remember { mutableStateOf(NoteForUi(
        id = 0,
        title = "Fetching...",
        summary = "...",
        content = "..."
    )) }

    var displayedTitle by remember { mutableStateOf(note.title) }
    var displayedContent by remember { mutableStateOf(note.content) }
    var coroutineScope = rememberCoroutineScope()

    fun saveNoteToDb(noteId: Long, displayedTitle: String, displayedContent: String) {
        coroutineScope.launch {
            notesDbViewModel.updateNoteContentById(noteId, displayedTitle, displayedContent)
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val fetchedNoteFromDb = notesDbViewModel.getNoteById(noteId.toLong())
            note = NoteForUi(
                id = fetchedNoteFromDb.id,
                title = fetchedNoteFromDb.title,
                summary = fetchedNoteFromDb.summary,
                content = fetchedNoteFromDb.content
            )
            displayedTitle = fetchedNoteFromDb.title
            displayedContent = fetchedNoteFromDb.content
        }
    }
    // var displayedContent = note.content
    // var title by remember { mutableStateOf(TextFieldValue("Europe Trip")) }
    // var content by remember { mutableStateOf(TextFieldValue("List of things to do in Europe\n\n• Colosseum\n• Decide Cities\n• Visa\n• Dates\n• Flights\n  • Go on 15th Jan?\n• Budget\n• Places to visit:\n  • A\n  • B\n  • C\n  • D")) }
    var showGenerateTasksPopup by remember { mutableStateOf(false) }

    // Use DisposableEffect to save the note when the composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            saveNoteToDb(note.id, displayedTitle, displayedContent)
        }
    }

    // Use OnLifecycleEvent to save the note when the lifecycle event occurs
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_ANY) {
                saveNoteToDb(note.id, displayedTitle, displayedContent)
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
            saveNoteToDb(note.id, displayedTitle, displayedContent)
        }
        navController.addOnDestinationChangedListener(callback)

        onDispose {
            navController.removeOnDestinationChangedListener(callback)
        }
    }

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
                Button(
                    onClick =
                    {
                        showGenerateTasksPopup = true
                        saveNoteToDb(note.id, displayedTitle, displayedContent)
                    },
                    modifier =
                    Modifier
                        .background(MaterialTheme
                        .colorScheme.primary, shape = RoundedCornerShape(8.dp))
                ) {
                    Text(text = "Generate Tasks", modifier = Modifier.padding(4.dp), color =
                    MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick =
                    {
                        navController.navigate(NotesDetailsScreenNav.PlanWithHalo.createRoute(noteId))
                        saveNoteToDb(note.id, displayedTitle, displayedContent)
                    },
                    modifier =
                Modifier.background
                    (MaterialTheme
                    .colorScheme
                    .primary, shape = RoundedCornerShape(8.dp))) {
                    Text(text = "Plan with Halo", modifier = Modifier.padding(4.dp), color =
                    MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            notesDbViewModel.deleteNoteById(noteId.toLong())
                            navController.popBackStack()
                        }
                              },
                    modifier = Modifier.size(45.dp)
                )
                {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Icon",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))) {
                BasicTextField(
                    value = displayedTitle,
                    onValueChange = { displayedTitle = it },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                )
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Star",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.summary,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f))
                }
                BasicTextField(
                    value = displayedContent,
                    onValueChange = { displayedContent = it },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                )
            }
            if (showGenerateTasksPopup) {
                NotesScreen_GenerateTasksPopup(noteId, onDismissRequest = { showGenerateTasksPopup =
                    false })
            }
        }
    }

    BackHandler {
        saveNoteToDb(note.id, displayedTitle, displayedContent)
        navController.popBackStack()
    }
}

