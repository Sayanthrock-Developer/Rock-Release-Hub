package com.sayanthrock.rockreleasehub

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sayanthrock.rockreleasehub.feature.auth.AuthScreen
import com.sayanthrock.rockreleasehub.feature.home.HomeScreen
import com.sayanthrock.rockreleasehub.feature.repositories.RepoListScreen
import com.sayanthrock.rockreleasehub.feature.repositories.RepoDetailsScreen
import com.sayanthrock.rockreleasehub.feature.workflows.WorkflowListScreen
import com.sayanthrock.rockreleasehub.feature.workflows.WorkflowDetailsScreen
import com.sayanthrock.rockreleasehub.feature.releases.ReleaseListScreen
import com.sayanthrock.rockreleasehub.feature.downloads.DownloadManagerScreen
import com.sayanthrock.rockreleasehub.feature.updates.UpdateScreen
import com.sayanthrock.rockreleasehub.feature.settings.SettingsScreen

enum class BottomNavRoute(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("home", "Home", Icons.Default.Home),
    REPOSITORIES("repositories", "Repos", Icons.Default.List),
    WORKFLOWS("workflows", "Workflows", Icons.Default.PlayArrow),
    DOWNLOADS("downloads", "Downloads", Icons.Default.KeyboardArrowDown),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = BottomNavRoute.entries.any { it.route == currentRoute }

    if (showBottomBar) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                val navigateTo = { route: String ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                item(
                    icon = { Icon(BottomNavRoute.HOME.icon, contentDescription = BottomNavRoute.HOME.title) },
                    label = { Text(BottomNavRoute.HOME.title) },
                    selected = currentRoute == BottomNavRoute.HOME.route,
                    onClick = { navigateTo(BottomNavRoute.HOME.route) }
                )
                item(
                    icon = { Icon(BottomNavRoute.REPOSITORIES.icon, contentDescription = BottomNavRoute.REPOSITORIES.title) },
                    label = { Text(BottomNavRoute.REPOSITORIES.title) },
                    selected = currentRoute == BottomNavRoute.REPOSITORIES.route,
                    onClick = { navigateTo(BottomNavRoute.REPOSITORIES.route) }
                )
                item(
                    icon = { Icon(BottomNavRoute.WORKFLOWS.icon, contentDescription = BottomNavRoute.WORKFLOWS.title) },
                    label = { Text(BottomNavRoute.WORKFLOWS.title) },
                    selected = currentRoute == BottomNavRoute.WORKFLOWS.route,
                    onClick = { navigateTo(BottomNavRoute.WORKFLOWS.route) }
                )
                item(
                    icon = { Icon(BottomNavRoute.DOWNLOADS.icon, contentDescription = BottomNavRoute.DOWNLOADS.title) },
                    label = { Text(BottomNavRoute.DOWNLOADS.title) },
                    selected = currentRoute == BottomNavRoute.DOWNLOADS.route,
                    onClick = { navigateTo(BottomNavRoute.DOWNLOADS.route) }
                )
                item(
                    icon = { Icon(BottomNavRoute.SETTINGS.icon, contentDescription = BottomNavRoute.SETTINGS.title) },
                    label = { Text(BottomNavRoute.SETTINGS.title) },
                    selected = currentRoute == BottomNavRoute.SETTINGS.route,
                    onClick = { navigateTo(BottomNavRoute.SETTINGS.route) }
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavHost(navController = navController)
            }
        }
    } else {
        AppNavHost(navController = navController)
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(onAuthSuccess = {
                navController.navigate(BottomNavRoute.HOME.route) {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }
        composable(BottomNavRoute.HOME.route) { HomeScreen() }
        composable(BottomNavRoute.REPOSITORIES.route) {
            RepoListScreen(onRepoClick = { repoId -> navController.navigate("repoDetails/$repoId") })
        }
        composable("repoDetails/{repoId}") { backStackEntry ->
            val repoId = backStackEntry.arguments?.getString("repoId")?.toLongOrNull() ?: return@composable
            RepoDetailsScreen(repoId = repoId, onBack = { navController.popBackStack() })
        }
        composable(BottomNavRoute.WORKFLOWS.route) {
            WorkflowListScreen(onWorkflowClick = { workflowId -> navController.navigate("workflowDetails/$workflowId") })
        }
        composable("workflowDetails/{workflowId}") { backStackEntry ->
            val workflowId = backStackEntry.arguments?.getString("workflowId")?.toLongOrNull() ?: return@composable
            WorkflowDetailsScreen(workflowId = workflowId, onBack = { navController.popBackStack() })
        }
        composable(BottomNavRoute.DOWNLOADS.route) { DownloadManagerScreen() }
        composable(BottomNavRoute.SETTINGS.route) { SettingsScreen() }
    }
}
