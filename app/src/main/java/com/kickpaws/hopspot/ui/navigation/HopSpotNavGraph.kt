package com.kickpaws.hopspot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kickpaws.hopspot.ui.screens.activityfeed.ActivityFeedScreen
import com.kickpaws.hopspot.ui.screens.admin.AdminScreen
import com.kickpaws.hopspot.ui.screens.auth.LoginScreen
import com.kickpaws.hopspot.ui.screens.auth.RegisterScreen
import com.kickpaws.hopspot.ui.screens.spotcreate.SpotCreateScreen
import com.kickpaws.hopspot.ui.screens.spotdetail.SpotDetailScreen
import com.kickpaws.hopspot.ui.screens.spotedit.SpotEditScreen
import com.kickpaws.hopspot.ui.screens.spotlist.SpotListScreen
import com.kickpaws.hopspot.ui.screens.favorites.FavoritesScreen
import com.kickpaws.hopspot.ui.screens.map.MapScreen
import com.kickpaws.hopspot.ui.screens.profile.ProfileScreen
import com.kickpaws.hopspot.ui.screens.splash.SplashScreen
import com.kickpaws.hopspot.ui.screens.visits.VisitsScreen

@Composable
fun HopSpotNavGraph(
    modifier: Modifier = Modifier,
    navController : NavHostController,
    startDestination : String = Route.Splash.route,
    deepLinkSpotId: Int? = null
){
    LaunchedEffect(deepLinkSpotId) {
        deepLinkSpotId?.let { spotId ->
            navController.navigate(Route.SpotDetail.createRoute(spotId.toString()))
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

        composable(route = Route.SpotList.route) {
            SpotListScreen(
                onSpotClick = { spotId ->
                    navController.navigate(Route.SpotDetail.createRoute(spotId.toString()))
                },
                onCreateSpotClick = {
                    navController.navigate(Route.SpotCreate.route)
                }
            )
        }

        composable(
            route = Route.SpotDetail.route,
            arguments = listOf(navArgument("spotId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getInt("spotId") ?: return@composable
            SpotDetailScreen(
                spotId = spotId,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Route.SpotEdit.createRoute(id.toString()))
                }
            )
        }

        composable(route = Route.SpotCreate.route) {
            SpotCreateScreen(
                onNavigateBack = { navController.popBackStack() },
                onSpotCreated = { spotId ->
                    navController.navigate(Route.SpotDetail.createRoute(spotId.toString())) {
                        popUpTo(Route.SpotCreate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Route.SpotEdit.route,
            arguments = listOf(navArgument("spotId") { type = NavType.IntType })
        ) { backStackEntry ->
            val spotId = backStackEntry.arguments?.getInt("spotId") ?: return@composable
            SpotEditScreen(
                spotId = spotId,
                onNavigateBack = { navController.popBackStack() },
                onDeleted = {
                    navController.navigate(Route.SpotList.route) {
                        popUpTo(Route.SpotList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Route.Map.route){
            MapScreen(
                onSpotClick = { spotId ->
                    navController.navigate(Route.SpotDetail.createRoute(spotId.toString()))
                },
                onCreateSpotClick = {
                    navController.navigate(Route.SpotCreate.route)
                },
                onActivityFeedClick = {
                    navController.navigate(Route.ActivityFeed.route)
                }
            )
        }

        composable(route = Route.ActivityFeed.route) {
            ActivityFeedScreen(
                onSpotClick = { spotId ->
                    navController.navigate(Route.SpotDetail.createRoute(spotId.toString()))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Route.Visits.route) {
            VisitsScreen(
                onSpotClick = { spotId ->
                    navController.navigate(Route.SpotDetail.createRoute(spotId.toString()))
                }
            )
        }

        composable(route = Route.Favorites.route) {
            FavoritesScreen(
                onSpotClick = { spotId ->
                    navController.navigate(Route.SpotDetail.createRoute(spotId.toString()))
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
