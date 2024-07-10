package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme
import xyz.haloai.haloai_android_productivity.ui.viewmodel.NotesDbViewModel
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.NotesDetailsScreenNav

@Composable
fun NotesScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val notesDbViewModel: NotesDbViewModel = koinInject()
    // Initialize notes list with empty list
    val notesToDisplay = remember { mutableStateListOf<NoteForUi>() }
    val coroutineScope = rememberCoroutineScope()
    var refreshData by remember { mutableStateOf(false) }

    suspend fun loadNotes() {
        coroutineScope.launch {
            // delay(500) // Wait for any changes to propagate
            notesToDisplay.clear()
            var allNotes = notesDbViewModel.getAllNotes()
            for (note in allNotes) {
                notesToDisplay.add(
                    NoteForUi(
                        id = note.id,
                        title = note.title,
                        summary = note.summary,
                        content = note.content
                    )
                )
            }
            delay(500) // If any updates, we wait for them to propagate
            allNotes = notesDbViewModel.getAllNotes()
            val notesToDisplay2 = mutableListOf<NoteForUi>()
            for (note in allNotes) {
                notesToDisplay2.add(
                    NoteForUi(
                        id = note.id,
                        title = note.title,
                        summary = note.summary,
                        content = note.content
                    )
                )
            }
            notesToDisplay.clear()
            notesToDisplay.addAll(notesToDisplay2)
        }
    }

    LaunchedEffect(Unit) {
        loadNotes()
    }

    LaunchedEffect(refreshData) {
        if (refreshData)
        {
            loadNotes()
            refreshData = false
        }
    }

    /*// Add a listener to refresh the notes list when the user navigates back to the Notes screen
    DisposableEffect(navController) {
        val callback = NavController.OnDestinationChangedListener { controller, destination, arguments ->
            if (destination.route == Screens.Notes.route) {
                loadNotes()
            }
        }
        navController.addOnDestinationChangedListener(callback)

        onDispose {
            navController.removeOnDestinationChangedListener(callback)
        }
    }*/

    HaloAI_Android_ProductivityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 60.dp) // Padding to prevent content from hiding behind the search bar
                ) {
                    for(note in notesToDisplay) {
                        NoteItem(note, navController)
                    }
                }

                SearchBarForNotes(modifier = Modifier.align(Alignment.BottomCenter))

                FloatingActionButton(
                    onClick = {
                          // Add a dummy note to the list
                        coroutineScope.launch {
                            // TODO: Replace with flow to allow text entry for a new note
                            notesDbViewModel.insert("Europe Trip", "List of things to do in Europe\n\n• Colosseum\n• Decide Cities\n• Visa\n• Dates\n• Flights\n  • Go on 15th Jan?\n• Budget\n• Places to visit:")
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
            }
        }
    }
}

@Composable
fun SearchBarForNotes(modifier: Modifier) {
    var searchState by remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()
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
                imageVector = Icons.Default.Search,
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
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .padding(8.dp)
                    .size(30.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_mic_24),
                    contentDescription = "Voice Icon",
                )
            }

            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .padding(8.dp)
                    .size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Arrow Icon"
                )
            }

        }
    }
}

@Composable
fun NoteItem(note: NoteForUi, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { navController.navigate(NotesDetailsScreenNav.Main.createRoute(note.id.toString())) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = note.title,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = note.summary,
                fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                fontSize = 12.sp,
                // modifier = Modifier.padding(bottom = 4.dp)
            )
            /*Text(
                text = "View more",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )*/
        }
    }
}

data class NoteForUi(
    val id: Long,
    val title: String,
    val summary: String,
    val content: String
)

