package com.berlintransit.app.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.berlintransit.app.AppContainer
import com.berlintransit.app.ui.board.BoardScreen
import com.berlintransit.app.ui.home.HomeScreen
import com.berlintransit.app.ui.search.SearchScreen

@Composable
fun TransitApp(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                container = container,
                onOpenStation = { id, name ->
                    navController.navigate(boardRoute(id, name))
                },
                onOpenSearch = { navController.navigate("search") }
            )
        }
        composable("search") {
            SearchScreen(
                container = container,
                onBack = { navController.popBackStack() },
                onOpenStation = { id, name ->
                    navController.navigate(boardRoute(id, name))
                }
            )
        }
        composable(
            route = "board/{stopId}?name={name}",
            arguments = listOf(
                navArgument("stopId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val stopId = backStackEntry.arguments?.getString("stopId").orEmpty()
            val stopName = backStackEntry.arguments?.getString("name").orEmpty()
            BoardScreen(
                container = container,
                stopId = stopId,
                stopName = stopName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun boardRoute(stopId: String, name: String): String =
    "board/$stopId?name=${Uri.encode(name)}"
