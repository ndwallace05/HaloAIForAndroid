package xyz.haloai.haloai_android_productivity.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType
import xyz.haloai.haloai_android_productivity.data.ui.viewmodel.EmailDbViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.GmailViewModel

@Composable
fun ChooseGoogleAccountsDialog(onDismissRequest: () -> Unit) {
    val emailDbViewModel: EmailDbViewModel = koinViewModel()
    val gmailViewModel: GmailViewModel = koinViewModel()
    val context = LocalContext.current
    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // User granted consent, notify the ViewModel
            emailDbViewModel.onConsentGranted()
        } else {
            // User denied consent, handle accordingly
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
    val accounts = emailDbViewModel.getAllGoogleAccountsOnDevice(context)
    val selectedAccounts = remember { mutableStateOf(accounts.map { false }.toList()) }
    var accountsAdded by remember {
        mutableStateOf(emptyList<String>())
    }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect (Unit) {
        coroutineScope.launch {
            accountsAdded = emailDbViewModel.getGoogleAccountsAdded()
        }
    }
    // List of boolean values to keep track of whether the account is already added
    val isAccountAdded = accounts.map { accountsAdded.contains(it) }
    var calendarIds by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
    val isCalendarIdSelected = remember { mutableStateOf<List<Boolean>?>(null) }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var selectedEmailId by remember {
        mutableStateOf(String())
    }

    val requestAuthLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            // Authorization intent returned successfully, retry the operation
            coroutineScope.launch {
                isLoading = true
                calendarIds = gmailViewModel.getCalendarIdsForGoogleAccount(context, selectedEmailId)
            }
        } else {
            // Handle failure or cancellation of the authorization intent
            // Optional: Show a message or take alternative actions
            Toast.makeText(context, "Permission Denied.", Toast.LENGTH_SHORT).show()
        }
    }

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
                item {
                    Text(
                        text = "Gmail Accounts",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .alpha(0.8f)
                    )
                }

                if (calendarIds == null)
                {
                    if (isLoading) { // Show a loading indicator while fetching calendar IDs
                        item {
                            CircularProgressIndicator()
                        }
                    }
                    else {
                        items(accounts.size) { idx ->
                            LaunchedEffect (key1 = selectedEmailId) {
                                if (selectedEmailId == accounts[idx]) {
                                    coroutineScope.launch {
                                        isLoading = true
                                        try {
                                            calendarIds =
                                                gmailViewModel.getCalendarIdsForGoogleAccount(
                                                    context,
                                                    accounts[idx]
                                                )
                                        } catch (e: UserRecoverableAuthIOException) {
                                            // User has not granted consent yet, it's a user
                                            // recoverable error
                                            requestAuthLauncher.launch(e.intent)
                                        } catch (e: Exception) {
                                            // Handle other exceptions (network errors, etc.)
                                            // e.printStackTrace()
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                e.message,
                                                //"Error fetching calendar IDs",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        // calendarIds = emailDbViewModel.getCalendarIdsForGoogleAccount(context, accounts[idx])!!
                                        isCalendarIdSelected.value = calendarIds!!.map { false }
                                    }
                                }
                            }
                            val modifier = Modifier.clickable {
                                selectedAccounts.value = selectedAccounts.value.toMutableList().apply {
                                    this[idx] = !this[idx]
                                }
                            }
                            EmailIdCard(
                                emailId = accounts[idx],
                                isAdded = isAccountAdded[idx],
                                modifier = modifier,
                                onClick = {
                                    selectedAccounts.value = selectedAccounts.value.toMutableList().apply {
                                        this[idx] = !this[idx]
                                    }
                                    selectedEmailId = accounts[idx]
                                })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                else
                {
                    items(calendarIds!!.size) { idx ->
                        val modifier = Modifier.clickable {
                            isCalendarIdSelected.value =
                                isCalendarIdSelected.value!!.toMutableList().apply {
                                    this[idx] = !this[idx]
                                }
                        }
                        CalendarIdCard(
                            calIdName = calendarIds!![idx].second,
                            isSelected = isCalendarIdSelected
                                .value!![idx],
                            modifier = modifier,
                            onClick = {
                            isCalendarIdSelected.value =
                                isCalendarIdSelected.value!!.toMutableList().apply {
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
                                for (i in 0 until calendarIds!!.size) {
                                    if (isCalendarIdSelected.value!![i]) {
                                        finalCalendarIdsSelected.add(calendarIds!![i].first)
                                    }
                                }
                                // Clear calendar IDs and email ID
                                calendarIds = null
                                isLoading = false
                                // Insert the selected email ID and calendar IDs into the database
                                emailDbViewModel.insert(selectedEmailId, finalCalendarIdsSelected,
                                    enumEmailType.GMAIL)
                                onDismissRequest()
                             },
                            modifier = Modifier.padding(8.dp).background(MaterialTheme
                                .colorScheme.primary, shape = RoundedCornerShape(8.dp))
                        )
                        {
                            Text("Add", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmailIdCard(emailId: String, isAdded: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isAdded,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            ),
            onClick = { onClick() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(emailId, modifier = Modifier.padding(1.dp), color = MaterialTheme.colorScheme
            .onPrimaryContainer, fontSize = 12.sp)
    }
}

@Composable
fun CalendarIdCard(calIdName: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unselectedColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            ),
            onClick = { onClick() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(calIdName, modifier = Modifier.padding(1.dp), color = MaterialTheme.colorScheme
            .onPrimaryContainer, fontSize = 12.sp)
    }
}

