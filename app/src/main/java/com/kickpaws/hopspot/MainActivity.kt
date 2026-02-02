package com.kickpaws.hopspot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kickpaws.hopspot.ui.components.BottomNavigationBar
import com.kickpaws.hopspot.ui.navigation.HopSpotNavGraph
import com.kickpaws.hopspot.ui.navigation.Route
import com.kickpaws.hopspot.ui.theme.Brown700
import com.kickpaws.hopspot.ui.theme.HopSpotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge mit dunklen System Bars
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Brown700.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Brown700.toArgb())
        )

        setContent {
            HopSpotTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in listOf(
                    Route.Map.route,
                    Route.BenchList.route,
                    Route.Visits.route,
                    Route.Profile.route
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    HopSpotNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}