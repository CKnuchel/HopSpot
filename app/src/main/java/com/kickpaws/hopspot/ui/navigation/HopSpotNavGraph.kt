package com.kickpaws.hopspot.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kickpaws.hopspot.ui.screens.admin.AdminScreen
import com.kickpaws.hopspot.ui.screens.auth.LoginScreen
import com.kickpaws.hopspot.ui.screens.auth.RegisterScreen
import com.kickpaws.hopspot.ui.screens.benchcreate.BenchCreateScreen
import com.kickpaws.hopspot.ui.screens.benchdetail.BenchDetailScreen
import com.kickpaws.hopspot.ui.screens.benchedit.BenchEditScreen
import com.kickpaws.hopspot.ui.screens.benchlist.BenchListScreen
import com.kickpaws.hopspot.ui.screens.profile.ProfileScreen

@Composable
fun HopSpotNavGraph(
    modifier: Modifier = Modifier,
    navController : NavHostController,
    startDestination : String = Route.Login.route,
){
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ){
        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Map.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Register.route)
                }
            )
        }

        composable(Route.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Route.Map.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Route.BenchList.route) {
            BenchListScreen(
                onBenchClick = { benchId ->
                    navController.navigate(Route.BenchDetail.createRoute(benchId.toString()))
                },
                onCreateBenchClick = {
                    navController.navigate(Route.BenchCreate.route)
                }
            )
        }

        composable(
            route = Route.BenchDetail.route,
            arguments = listOf(navArgument("benchId") { type = NavType.IntType })
        ) { backStackEntry ->
            val benchId = backStackEntry.arguments?.getInt("benchId") ?: return@composable
            BenchDetailScreen(
                benchId = benchId,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Route.BenchEdit.createRoute(id.toString()))
                }
            )
        }

        composable(route = Route.BenchCreate.route) {
            BenchCreateScreen(
                onNavigateBack = { navController.popBackStack() },
                onBenchCreated = { benchId ->
                    navController.navigate(Route.BenchDetail.createRoute(benchId.toString())) {
                        popUpTo(Route.BenchCreate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Route.BenchEdit.route,
            arguments = listOf(navArgument("benchId") { type = NavType.IntType })
        ) { backStackEntry ->
            val benchId = backStackEntry.arguments?.getInt("benchId") ?: return@composable
            BenchEditScreen(
                benchId = benchId,
                onNavigateBack = { navController.popBackStack() },
                onDeleted = {
                    navController.navigate(Route.BenchList.route) {
                        popUpTo(Route.BenchList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Route.Map.route){
            Text("Map Screen")
        }

        composable(route = Route.Visits.route){
            Text("Visits Screen")
        }

        composable(Route.Profile.route) {
            ProfileScreen(
                onLoggedOut = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Admin.route) {
            AdminScreen()
        }
    }
}