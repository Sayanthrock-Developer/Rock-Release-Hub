package com.sayanthrock.rockreleasehub

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.sayanthrock.rockreleasehub.feature.releases.ReleaseListScreen
import com.sayanthrock.rockreleasehub.feature.downloads.DownloadManagerScreen
import com.sayanthrock.rockreleasehub.feature.apkinspector.ApkInspectorScreen
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = BottomNavRoute.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavRoute.entries.forEach { navItem ->
                        NavigationBarItem(
                            icon = { Icon(navItem.icon, contentDescription = navItem.title) },
                            label = { Text(navItem.title) },
                            selected = currentRoute == navItem.route,
                            onClick = {
                                navController.navigate(navItem.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(padding)
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
                RepoListScreen(onRepoClick = { repoId -> navController.navigate("repoDetails/\$repoId") })
            }
            composable("repoDetails/{repoId}") { backStackEntry ->
                val repoId = backStackEntry.arguments?.getString("repoId")?.toLongOrNull() ?: return@composable
                RepoDetailsScreen(repoId = repoId, onBack = { navController.popBackStack() })
            }
            composable(BottomNavRoute.WORKFLOWS.route) {
                WorkflowListScreen(onWorkflowClick = { /* Handle click */ })
            }
            composable(BottomNavRoute.DOWNLOADS.route) { DownloadManagerScreen() }
            composable(BottomNavRoute.SETTINGS.route) { SettingsScreen() }
        }
    }
}
