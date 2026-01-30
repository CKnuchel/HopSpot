package com.kickpaws.hopspot.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

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
        composable(route = Route.Login.route){
            Text("Login Screen")
        }

        composable(route = Route.Register.route){
            Text("Register Screen")
        }

        composable(route = Route.BenchList.route){
            Text("Bench List Screen")
        }

        composable(route = Route.BenchDetail.route,
            arguments = listOf(navArgument("benchId") {type = NavType.StringType})
        ){ backStackEntry ->
            val benchId = backStackEntry.arguments?.getString("benchId") ?: ""
            Text("Bench Detail: $benchId")
        }

        composable(route = Route.BenchCreate.route){
            Text("Bench Create Screen")
        }

        composable(route = Route.BenchEdit.route,
            arguments = listOf(navArgument("benchId") {type = NavType.StringType})
        ){ backStackEntry ->
            val benchId = backStackEntry.arguments?.getString("benchId") ?: ""
            Text("Bench Edit: $benchId")
        }

        composable(route = Route.Map.route){
            Text("Map Screen")
        }

        composable(route = Route.Visits.route){
            Text("Visits Screen")
        }

        composable(route = Route.Profile.route){
            Text("Profile Screen")
        }
    }
}