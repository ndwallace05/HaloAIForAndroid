package xyz.haloai.haloai_android_productivity.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.ui.theme.HaloAI_Android_ProductivityTheme

@Composable
fun TasksScreen(navController: NavController) {
    HaloAI_Android_ProductivityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        TaskList()
                    }

                }

                SearchBarForTasks(modifier = Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

@Composable
fun TaskList() {
    Column {
        repeat(12) {
            TaskItem(taskName = "Sample Task 1", taskDescription = "Sample Description 1")
        }
    }
}

@Composable
fun TaskItem(taskName: String, taskDescription: String) {
    var isChecked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it }
            )
        }
        Column (modifier = Modifier.weight(3f)) {
            Text(
                text = taskName,
                fontSize = 16.sp,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(
                text = taskDescription,
                fontSize = 12.sp,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
        }
        Row {
            IconButton(onClick = { /* Handle schedule click */ }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Schedule")
            }
            IconButton(onClick = { /* Handle delete click */ }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun SearchBarForTasks(modifier: Modifier) {
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

