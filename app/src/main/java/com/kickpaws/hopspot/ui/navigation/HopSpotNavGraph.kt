package com.kickpaws.hopspot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.kickpaws.hopspot.ui.screens.map.MapScreen
import com.kickpaws.hopspot.ui.screens.profile.ProfileScreen
import com.kickpaws.hopspot.ui.screens.splash.SplashScreen
import com.kickpaws.hopspot.ui.screens.visits.VisitsScreen

@Composable
fun HopSpotNavGraph(
    modifier: Modifier = Modifier,
    navController : NavHostController,
    startDestination : String = Route.Splash.route,
    deepLinkBenchId: Int? = null
){
    LaunchedEffect(deepLinkBenchId) {
        deepLinkBenchId?.let { benchId ->
            navController.navigate(Route.BenchDetail.createRoute(benchId.toString()))
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ){
        composable(Route.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Route.Map.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            )
        }

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
            MapScreen(
                onBenchClick = { benchId ->
                    navController.navigate(Route.BenchDetail.createRoute(benchId.toString()))
                },
                onCreateBenchClick = {
                    navController.navigate(Route.BenchCreate.route)
                }
            )
        }

        composable(route = Route.Visits.route) {
            VisitsScreen(
                onBenchClick = { benchId ->
                    navController.navigate(Route.BenchDetail.createRoute(benchId.toString()))
                }
            )
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