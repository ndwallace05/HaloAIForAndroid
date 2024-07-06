package xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity

// Main NavBars for the app
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.AssistantScreen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.CalendarScreen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.CustomizeLTGoalScreen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.HomeScreen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.NoteDetailsScreen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.NotesScreen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.PlanWithHaloScreen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.PlanWithHalo_Notes_Screen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.ProfileScreen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.SettingsScreen
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.TasksScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarForApp(title: String,
                 scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                 navigateToPreviousScreen: () -> Unit = {},
                 navController: NavHostController) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
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
            if (navController.currentDestination?.route != Screens.Home.route)
            {
                IconButton(onClick = {
                    navigateToPreviousScreen()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                navController.navigate(Screens.Profile.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }

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

sealed class Screens(val route: String, val label: String) {
    object Home : Screens("home_route", label = "Home")
    object Calendar : Screens("calendar_route", label = "Calendar")
    object Assistant : Screens("assistant_route", label = "Assistant")
    object Notes : Screens("notes_route", label = "Notes")
    object More : Screens("more_route", label = "More")
    object Tasks : Screens("tasks_route", label = "Tasks")
    object PlanWithHalo : Screens("plan_with_halo_route", label = "Plan with Halo")
    object Profile : Screens("profile_route", label = "Profile")
    object Settings : Screens("settings_route", label = "Settings")
    object NoteDetails : Screens("note_details_route/{noteId}", label = "Note Details")
    object PlanWithHalo_NoteDetails : Screens("note_details_plan_with_halo_route/{noteId}", label
    = "Plan with Halo - Notes")

    object CustomizeLTGoal : Screens("customize_lt_goal_route/{ltGoalId}", label = "Customize Plan")

}

sealed class NotesDetailsScreenNav(val route: String) {
    object Main: NotesDetailsScreenNav("note_details_route/{noteId}") {
        fun createRoute(noteId: String): String = "note_details_route/$noteId"
    }

    object PlanWithHalo: NotesDetailsScreenNav("note_details_plan_with_halo_route/{noteId}") {
        fun createRoute(noteId: String): String = "note_details_plan_with_halo_route/${noteId}"
    }
}

sealed class PlanWithHaloScreenNav(val route: String) {

    object CustomizeLTGoal: PlanWithHaloScreenNav("customize_lt_goal_route/{ltGoalId}") {
        fun createRoute(ltGoalId: String): String = "customize_lt_goal_route/${ltGoalId}"
    }
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
                route = Screens.Tasks.route
            ),
            BottomNavigationItem(
                label = "Plan with Halo",
                iconRes = R.drawable.long_term_goals,
                route = Screens.PlanWithHalo.route
            ),
        )
    }

    fun topBarNavigationItems() : List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = "Profile",
                iconVector = Icons.Filled.AccountCircle,
                route = Screens.Home.route
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavBarsWithContent() {
    val navController = rememberNavController() // Navigation controller
    val navBackStackEntry by navController.currentBackStackEntryAsState() // Current backstack entry
    val sheetState = rememberModalBottomSheetState() // Modal bottom sheet state for more navigation items
    var showBottomSheet by remember { mutableStateOf(false) } // Show bottom sheet state variable
    val currentDestination = navBackStackEntry?.destination // Current destination
    val currentScreenTitle = when (navBackStackEntry?.destination?.route) {
        Screens.Home.route -> Screens.Home.label
        Screens.Calendar.route -> Screens.Calendar.label
        Screens.Assistant.route -> Screens.Assistant.label
        Screens.Notes.route -> Screens.Notes.label
        Screens.More.route -> Screens.More.label
        Screens.Tasks.route -> Screens.Tasks.label
        Screens.PlanWithHalo.route -> Screens.PlanWithHalo.label
        Screens.Profile.route -> Screens.Profile.label
        Screens.NoteDetails.route -> Screens.NoteDetails.label
        Screens.PlanWithHalo_NoteDetails.route -> Screens.PlanWithHalo_NoteDetails.label
        Screens.Settings.route -> Screens.Settings.label
        Screens.CustomizeLTGoal.route -> Screens.CustomizeLTGoal.label
        else -> "Halo AI"
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBarForApp(
                currentScreenTitle,
                navigateToPreviousScreen =
                {
                    navController.popBackStack()
                },
                navController = navController
            )
        },
        bottomBar = {
            NavigationBar {
                BottomNavigationItem().bottomNavigationItems().forEachIndexed { _, navigationItem ->
                    NavigationBarItem(
                        selected = navigationItem.route == currentDestination?.route, // Check if the current destination is the same as the navigation item
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
                    navController = navController
                )
            }
            composable(Screens.Calendar.route) {
                CalendarScreen(
                    navController = navController
                )
            }
            composable(Screens.Assistant.route) {
                AssistantScreen(
                    navController = navController
                )
            }
            composable(Screens.Notes.route) {
                NotesScreen(
                    navController = navController
                )
            }
            composable(Screens.More.route) {
                showBottomSheet = true
            }
            composable(Screens.Profile.route) {
                ProfileScreen(
                    navController = navController
                )
            }
            composable(Screens.Tasks.route) {
                TasksScreen(
                    navController = navController
                )
            }
            composable(Screens.PlanWithHalo.route) {
                PlanWithHaloScreen(
                    navController = navController
                )
            }
            composable(
                Screens.NoteDetails.route,
                arguments = listOf(navArgument("noteId") {
                    defaultValue = "1"
                    type = NavType.StringType // Room db id for note
                })) {
                    backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                NoteDetailsScreen(navController = navController, noteId = noteId!!)
            }
            composable(
                Screens.PlanWithHalo_NoteDetails.route,
                arguments = listOf(navArgument("noteId") {
                defaultValue = "1"
                type = NavType.StringType // Room db id for note
            })) {
                backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                PlanWithHalo_Notes_Screen(navController = navController, noteId = noteId!!)
            }
            composable(Screens.Settings.route) {
                // Settings screen
                SettingsScreen(navController = navController)
            }
            composable(Screens.CustomizeLTGoal.route, arguments = listOf(navArgument("ltGoalId") {
                defaultValue = "1"
                type = NavType.StringType // Room db id for long term goal
            })) {
                backStackEntry ->
                val ltGoalId = backStackEntry.arguments?.getString("ltGoalId")
                // Customize long term goal screen
                CustomizeLTGoalScreen(navController = navController, ltGoalId = ltGoalId!!)
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
                                .weight(1F)
                                .align(Alignment.CenterVertically)
                                .clickable {
                                    navController.navigate(navigationItem.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    showBottomSheet = false
                                }
                        ){
                            if (navigationItem.iconRes != null) {
                                Icon(painter = painterResource(id = navigationItem.iconRes),
                                    contentDescription= navigationItem.label, modifier = Modifier
                                        .fillMaxHeight(0.05F)
                                        .align(Alignment.CenterHorizontally))
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