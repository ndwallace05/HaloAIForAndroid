package xyz.haloai.haloai_android_productivity.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import xyz.haloai.haloai_android_productivity.ui.theme.HaloAI_Android_ProductivityTheme

@Composable
fun SettingsScreen(navController: NavController) {

    var focusMode by remember { mutableStateOf(true) }
    var onDeviceML by remember { mutableStateOf(true) }
    var showDevOptions by remember { mutableStateOf(false) }

    HaloAI_Android_ProductivityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item {
                    SwitchItem(
                        title = "Focus Mode",
                        checked = focusMode,
                        onCheckedChange = { focusMode = it }
                    )
                }
                item {
                    SwitchItem(
                        title = "On-Device ML",
                        checked = onDeviceML,
                        onCheckedChange = { onDeviceML = it }
                    )
                }

                item{
                    Text(
                        text = "Permissions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item{
                    AllPermissionSwitches()
                }

                item {
                    Text(
                        text = "Developer Options",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    Button(
                        onClick = { showDevOptions = !showDevOptions },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(50.dp)
                            .clip(RoundedCornerShape(50))
                    ) {
                        val textForDevOptionsButton: String = if (showDevOptions) "Hide Developer Options" else "Show Developer Options"
                        Text(
                            text = textForDevOptionsButton,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                if (showDevOptions)
                {
                    item{
                        DeveloperOptions()
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperOptions() {
    var onDeviceML by remember { mutableStateOf(true) }
    SwitchItem(title = "On-Device ML", checked = onDeviceML, onCheckedChange = { onDeviceML = it })
}

@Composable
fun SwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun AllPermissionSwitches()
{
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PermissionSwitchItem(
            title = "Gallery",
            permission = android.Manifest.permission.READ_MEDIA_IMAGES
        )
    }
    else {
        PermissionSwitchItem(
            title = "Gallery",
            permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    PermissionSwitchItem(
        title = "Audio",
        permission = android.Manifest.permission.RECORD_AUDIO
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PermissionSwitchItem(
            title = "Notifications",
            permission = android.Manifest.permission.POST_NOTIFICATIONS
        )
    }
    PermissionSwitchItem(
        title = "Calendar",
        permission = android.Manifest.permission.READ_CALENDAR
    )
    PermissionSwitchItem(
        title = "Contacts",
        permission = android.Manifest.permission.READ_CONTACTS
    )
    PermissionSwitchItem(
        title = "Accounts",
        permission = android.Manifest.permission.GET_ACCOUNTS
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionSwitchItem(
    title: String,
    permission: String
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission)
    var isGranted by remember { mutableStateOf(permissionState.status.isGranted) }

    LaunchedEffect(permissionState.status) {
        isGranted = permissionState.status.isGranted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = isGranted,
            onCheckedChange = {
                if (permissionState.status.isGranted) {
                    openAppSettings(context)
                } else {
                    permissionState.launchPermissionRequest()
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}