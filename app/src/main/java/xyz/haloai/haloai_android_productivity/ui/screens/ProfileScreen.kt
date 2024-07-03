package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import xyz.haloai.haloai_android_productivity.data.ui.theme.HaloAI_Android_ProductivityTheme
import xyz.haloai.haloai_android_productivity.data.ui.viewmodel.EmailDbViewModel
import xyz.haloai.haloai_android_productivity.ui.screens.ChooseGoogleAccountsDialog
import xyz.haloai.haloai_android_productivity.ui.screens.SelectCalendarIdsDialog
import xyz.haloai.haloai_android_productivity.ui.viewmodel.MicrosoftGraphViewModel
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.Screens

@Composable
fun ProfileScreen(navController: NavController) {
    var showAddGoogleAccountsDialog by remember { mutableStateOf(false) }
    var addMicrosoftAccountsAuthFlowActive by remember { mutableStateOf(false) }
    var showCalIdsForMicrosoftAccountSelected by remember { mutableStateOf(false) }
    val emailDbViewModel: EmailDbViewModel = koinViewModel()
    val coroutineScope = rememberCoroutineScope()
    var accountsAdded by remember { mutableStateOf(emptyList<String>()) }
    var microsoftGraphViewModel: MicrosoftGraphViewModel = koinViewModel()
    val context = LocalContext.current
    var microsoftEmailId by remember { mutableStateOf("") }
    var microsoftCalendarIds = remember { mutableStateListOf<Pair<String, String>>() }
    var emailIdToDelete by remember { mutableStateOf("") }
    // var microsoftCalendarIds by remember { mutableStateOf(emptyList<Pair<String, String>>()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch { accountsAdded = emailDbViewModel.allEmailIds()}
    }

    LaunchedEffect (key1 = showAddGoogleAccountsDialog) {
        delay(1000) // Wait for any db operations to complete
        coroutineScope.launch { accountsAdded = emailDbViewModel.allEmailIds()}
    }

    LaunchedEffect (key1 = emailIdToDelete) {
        if (emailIdToDelete.isNotEmpty()) {
            coroutineScope.launch {
                emailDbViewModel.deleteById(emailIdToDelete)
                delay(1000)
                accountsAdded = emailDbViewModel.allEmailIds()
                emailIdToDelete = ""
            }
        }
    }

    LaunchedEffect (key1 = addMicrosoftAccountsAuthFlowActive) {
        if (addMicrosoftAccountsAuthFlowActive) {
            // Trigger the microsoft account addition flow
            coroutineScope.launch {
                try {
                    var calIdsWithEmail = microsoftGraphViewModel.getCalendarIdsForMicrosoftAccount(
                        context = context,
                        coroutineScope = coroutineScope
                    )
                    // Last element in the list is the email id
                    microsoftEmailId = calIdsWithEmail.last().second
                    calIdsWithEmail.removeLast()
                    microsoftCalendarIds.clear()
                    microsoftCalendarIds.addAll(calIdsWithEmail)
                    // Show the dialog to select calendar ids
                    showCalIdsForMicrosoftAccountSelected = true
                    addMicrosoftAccountsAuthFlowActive = false
                }
                catch (e: Exception) {
                    // Handle the exception
                    addMicrosoftAccountsAuthFlowActive = false
                }
            }
        }
    }

    LaunchedEffect(key1 = showCalIdsForMicrosoftAccountSelected) {
        delay(1000) // Wait for any db operations to complete
        if (showCalIdsForMicrosoftAccountSelected == false) {
            coroutineScope.launch { accountsAdded = emailDbViewModel.allEmailIds()}
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
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Button(
                            onClick = { navController.navigate(Screens.Settings.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View Settings", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item{
                        UserPersonaSection()
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        if (accountsAdded.isNotEmpty()) {
                            AccountsSection(
                                emails = accountsAdded,
                                onAccountDelete = {
                                    emailIdToDelete = it
                                }
                            )
                        }
                        else {
                            Text(
                                text = "No accounts added yet. Add your email accounts to get started.",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 16.sp
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(onClick = { showAddGoogleAccountsDialog = true }, colors =
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(8.dp)) {
                                Text("Add Gmail", color = MaterialTheme.colorScheme.onPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { addMicrosoftAccountsAuthFlowActive = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(8.dp)) {
                                Text("Add Microsoft", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }


                if (showAddGoogleAccountsDialog) {
                    ChooseGoogleAccountsDialog(
                        onDismissRequest = { showAddGoogleAccountsDialog = false }
                    )
                }
                if (showCalIdsForMicrosoftAccountSelected) {
                    SelectCalendarIdsDialog(
                        emailId = microsoftEmailId,
                        calendarIds = microsoftCalendarIds,
                        onDismissRequest = { showCalIdsForMicrosoftAccountSelected = false }
                    )

                }
            }
            if (addMicrosoftAccountsAuthFlowActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun UserPersonaSection() {
    val titles = listOf("Interests", "Preferences", "Halo AI")
    val descriptions = listOf(
        "Adi is interested in sports, and startups ...",
        "Adi prefers vegetarian food, and is ...",
        "Adi is the founder and CEO of Halo AI ..."
    )
    val onEdit: () -> Unit = { /*TODO*/ }
    val onDelete: () -> Unit = { /*TODO*/ }
    Column {
        for (i in 0..2) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = titles[i],
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Row {
                            IconButton(onClick = onEdit) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(16.dp)
                                )
                            }
                            IconButton(onClick = onDelete) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Text(
                        descriptions[i],
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .alpha(0.8f),
                        fontSize = 12.sp
                    )
                    // Spacer(modifier = Modifier.height(8.dp))
                    /*TextButton(onClick = { *//*TODO*//* }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)) {
                        Text("View more", color = MaterialTheme.colorScheme.onSecondary)
                    }*/
                }

            }
        }
    }
}

@Composable
fun AccountsSection(emails: List<String>, onAccountDelete: (String) -> Unit) {
    Column {
        Text(
            text = "Accounts",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        emails.forEach { email ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = email, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onAccountDelete(email) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

