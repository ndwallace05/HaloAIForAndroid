package xyz.haloai.haloai_android_productivity

// Main NavBars for the app
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.haloai.haloai_android_productivity.screens.AssistantScreen
import xyz.haloai.haloai_android_productivity.screens.CalendarScreen
import xyz.haloai.haloai_android_productivity.screens.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarForApp(title: String, scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults
    .pinnedScrollBehavior(), navigateToPreviousScreen: () -> Unit = {}) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                navigateToPreviousScreen
            }) {
                 Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back"
                )
            }
        },
        actions = {
            IconButton(onClick = {
                // TODO: Launch Profile screen
            }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Localized description"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

sealed class Screens(val route: String) {
    object Home : Screens("home_route")
    object Calendar : Screens("calendar_route")
    object Assistant : Screens("assistant_route")
    object Notes : Screens("notes_route")
    object More : Screens("more_route")
}

// Defines Main Flows for the app
data class BottomNavigationItem(
    val label : String = "",
    val iconVector: ImageVector? = null,
    val iconRes: Int? = null,
    val route : String = ""
) {
    fun bottomNavigationItems() : List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = "Home",
                iconVector = Icons.Filled.Home,
                route = Screens.Home.route
            ),
            BottomNavigationItem(
                label = "Calendar",
                iconVector = Icons.Filled.DateRange,
                route = Screens.Calendar.route
            ),
            BottomNavigationItem(
                label = "Assistant",
                iconRes = R.drawable.haloai_logo,
                route = Screens.Assistant.route
            ),
            BottomNavigationItem(
                label = "Notes",
                iconRes = R.drawable.notes,
                route = Screens.Notes.route
            ),
            BottomNavigationItem(
                label = "More",
                iconVector = Icons.Filled.Menu,
                route = Screens.More.route
            ),
        )
    }

    fun moreNavigationItems() : List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = "Tasks",
                iconRes = R.drawable.tasks,
                route = Screens.Home.route
            ),
            BottomNavigationItem(
                label = "Plan with Halo",
                iconRes = R.drawable.long_term_goals,
                route = Screens.Calendar.route
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavBarsWithContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val currentDestination = navBackStackEntry?.destination
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomNavigationItem().bottomNavigationItems().forEachIndexed { _, navigationItem ->
                    NavigationBarItem(
                        selected = navigationItem.route == currentDestination?.route,
                        label = {
                            Text(navigationItem.label)
                        },
                        icon = {
                            if (navigationItem.iconVector != null) {
                                // Use ImageVector
                                Icon(imageVector = navigationItem.iconVector, contentDescription
                                = navigationItem.label)
                            } else if (navigationItem.iconRes != null) {
                                // Use resource ID
                                Icon(painter = painterResource(id = navigationItem.iconRes),
                                    contentDescription= navigationItem.label, modifier = Modifier.fillMaxWidth(0.3F))
                            }
                        },
                        onClick = {
                            if (navigationItem.route == Screens.More.route) {
                                showBottomSheet = true
                            } else {
                                navController.navigate(navigationItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screens.Home.route,
            modifier = Modifier.padding(paddingValues = paddingValues)) {
            composable(Screens.Home.route) {
                HomeScreen(
                    navController
                )
            }
            composable(Screens.Calendar.route) {
                CalendarScreen(
                    navController
                )
            }
            composable(Screens.Assistant.route) {
                AssistantScreen(
                    navController
                )
            }
            composable(Screens.Notes.route) {
                //
            }
            composable(Screens.More.route) {
                showBottomSheet = true
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                // Sheet content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    BottomNavigationItem().moreNavigationItems().forEachIndexed { _, navigationItem ->
                        Column(
                            modifier = Modifier
                                .weight(1F).align(Alignment.CenterVertically)
                                .clickable {  } //
                        // Create a nav 
                        // action
                        ){
                            if (navigationItem.iconRes != null) {
                                Icon(painter = painterResource(id = navigationItem.iconRes),
                                    contentDescription= navigationItem.label, modifier = Modifier
                                        .fillMaxHeight(0.05F).align(Alignment.CenterHorizontally))
                            }
                            else {
                                Icon(imageVector = navigationItem.iconVector!!, contentDescription
                                = navigationItem.label, modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                )
                            }
                            Text(navigationItem.label, modifier = Modifier
                                .align(Alignment.CenterHorizontally))
                        }
                    }
                }
            }
        }
    }
}